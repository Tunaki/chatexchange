package fr.tunaki.stackoverflow.chat;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.tunaki.stackoverflow.chat.event.Event;
import fr.tunaki.stackoverflow.chat.event.EventType;
import fr.tunaki.stackoverflow.chat.event.Events;

public final class Room {

	private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);

	private static final String SUCCESS = "ok";
	private static final Pattern TRY_AGAIN_PATTERN = Pattern.compile("You can perform this action again in (\\d+) seconds");
	private static final int NUMBER_OF_RETRIES_ON_THROTTLE = 5;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final ExecutorService eventExecutor = Executors.newCachedThreadPool();

	private Session websocketSession;
	private Map<EventType<Object>, List<Consumer<Object>>> chatEventListeners = new HashMap<>();

	private long roomId;
	private String host;
	private String fkey;

	private HttpClient httpClient;
	private Map<String, String> cookies;

	private boolean hasLeft = false;

	Room(String host, long roomId, HttpClient httpClient, Map<String, String> cookies) {
		this.roomId = roomId;
		this.host = host;
		this.httpClient = httpClient;
		this.cookies = new HashMap<>(cookies);
		fkey = retrieveFKey(roomId);
		executor.scheduleAtFixedRate(() -> fkey = retrieveFKey(roomId), 1, 1, TimeUnit.HOURS);
		startWebsocket();
	}

	private JsonElement post(String url, String... data) {
		return post(NUMBER_OF_RETRIES_ON_THROTTLE, url, data);
	}
	
	private JsonElement post(int retryCount, String url, String... data) {
		Response response;
		try {
			response = httpClient.postIgnoringErrors(url, cookies, withFkey(data));
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		String body = response.body();
		if (response.statusCode() == 200) {
			return new JsonParser().parse(body);
		}
		Matcher matcher = TRY_AGAIN_PATTERN.matcher(body);
		if (retryCount > 0 && matcher.find()) {
			int throttle = Integer.parseInt(matcher.group(1));
			LOGGER.debug("Tried to POST to URL {} with data {} but was throttled, retrying in {} seconds", url, data, throttle);
			try {
				Thread.sleep(1000 * throttle);
			} catch (InterruptedException e) { }
			return post(retryCount - 1, url, data);
		} else {
			throw new ChatOperationException("The chat operation failed with the message: " + body);
		}
	}

	private String[] withFkey(String[] data) {
		String[] dataWithFKey = new String[data.length + 2];
		dataWithFKey[0] = "fkey";
		dataWithFKey[1] = fkey;
		System.arraycopy(data, 0, dataWithFKey, 2, data.length);
		return dataWithFKey;
	}

	private String retrieveFKey(long roomId) {
		try {
			Response response = httpClient.get("http://chat." + host + "/rooms/" + roomId, cookies);
			String fkey = response.parse().getElementById("fkey").val();
			LOGGER.debug("New fkey retrieved for room {}", roomId);
			return fkey;
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
	}

	private void startWebsocket() {
		String websocketUrl = post("http://chat." + host + "/ws-auth", "roomid", String.valueOf(roomId)).getAsJsonObject().get("url").getAsString();
		String time = post("http://chat." + host + "/chats/" + roomId + "/events").getAsJsonObject().get("time").getAsString();
		ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
		Builder configBuilder = ClientEndpointConfig.Builder.create();
		configBuilder.configurator(new Configurator() {
			@Override
			public void beforeRequest(Map<String, List<String>> headers) {
				headers.put("Origin", Arrays.asList("http://chat." + host));
			}
		});
		websocketUrl += "?l=" + time;
		LOGGER.debug("Connecting to chat websocket " + websocketUrl);
		try {
			websocketSession = client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session session, EndpointConfig config) {
					session.addMessageHandler(String.class, Room.this::handleChatEvent);
				}
				@Override
				public void onError(Session session, Throwable thr) {
					LOGGER.error("An error occured during the processing of a message", thr);
				}
			}, configBuilder.build(), new URI(websocketUrl));
		} catch (DeploymentException | URISyntaxException | IOException e) {
			throw new ChatOperationException("Cannot connect to chat websocket", e);
		}
	}

	private void handleChatEvent(String json) {
		LOGGER.debug("Received message: " + json);
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		jsonObject.entrySet().stream().filter(e -> e.getKey().equals("r" + roomId)).map(Map.Entry::getValue).map(JsonElement::getAsJsonObject).map(o -> o.get("e")).filter(Objects::nonNull).map(JsonElement::getAsJsonArray).findFirst().ifPresent(events -> {
			for (Event event : Events.fromJsonData(events, roomId)) {
				for (Consumer<Object> listener : chatEventListeners.getOrDefault(EventType.fromEvent(event), Collections.emptyList())) {
					eventExecutor.submit(() -> listener.accept(event));
				}
			}
		});
	}

	/**
	 * Adds a listener for the given event. Valid events are defined as constants of the {@link EventType} class.
	 * <p>All listeners bound to a specific event will be called when the corresponding event is raised.
	 * @param event Event to listen to.
	 * @param listener Listener to add to this event.
	 */
	public <T> void addEventListener(EventType<T> event, Consumer<T> listener) {
		@SuppressWarnings("unchecked") EventType<Object> eventCast = (EventType<Object>) event;
		@SuppressWarnings("unchecked") Consumer<Object> listenerCast = (Consumer<Object>) listener;
		chatEventListeners.computeIfAbsent(eventCast, e -> new ArrayList<>()).add(listenerCast);
	}
	
	private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, executor).whenComplete((res, thr) -> {
			if (res != null) LOGGER.trace("Task completed successfully with result: " + res);
			if (thr != null) LOGGER.error("Couldn't execute task", thr);
		});
	}

	/**
	 * Sends the given message asynchronously.
	 * @param message Content of the message to send.
	 * @return A future holding the id of the sent message.
	 */
	public CompletableFuture<Long> send(String message) {
		LOGGER.info("Task added - sending message '{}' to room {}.", message, roomId);
		return supplyAsync(() -> {
			JsonElement element = post("http://chat." + host + "/chats/" + roomId + "/messages/new", "text", message);
			LOGGER.debug("Message '{}' sent to room {}, raw result: {}", message, roomId, element);
			return element.getAsJsonObject().get("id").getAsLong();
		});
	}

	/**
	 * Sends a reply message to the given message id.
	 * @param messageId Id of the message to reply to.
	 * @param message Message consisting of the reply.
	 * @return A future holding the id of the newly sent message.
	 */
	public CompletableFuture<Long> replyTo(long messageId, String message) {
		return send(":" + messageId + " " + message);
	}

	/**
	 * Edits asynchronously the message having the given id with the new given content.
	 * @param messageId Id of the message to edit.
	 * @param message New content of the message.
	 * @return A future holding the id of the edited message (which is the same as the given message id).
	 */
	public CompletableFuture<Long> edit(long messageId, String message) {
		LOGGER.info("Task added - editing message {} in room {}.", messageId, roomId);
		return supplyAsync(() -> {
			String result = post("http://chat." + host + "/messages/" + messageId, "text", message).getAsString();
			LOGGER.debug("Message {} edited to '{}' in room {}, raw result: {}", messageId, message, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot edit message " + messageId + ". Reason: " + result);
			}
			return messageId;
		});
	}

	/**
	 * Deletes asynchronously the message having the given id.
	 * @param messageId Id of the message to delete.
	 * @return A future holding no value.
	 */
	public CompletableFuture<Void> delete(long messageId) {
		LOGGER.info("Task added - deleting message {} in room {}.", messageId, roomId);
		return supplyAsync(() -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/delete").getAsString();
			LOGGER.debug("Message {} deleted in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot delete message " + messageId + ". Reason: " + result);
			}
			return null;
		});
	}

	public CompletableFuture<Void> toggleStar(long messageId) {
		LOGGER.info("Task added - starring/unstarring message {} in room {}.", messageId, roomId);
		return supplyAsync(() -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/star").getAsString();
			LOGGER.debug("Message {} starred/unstarred in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot star/unstar message " + messageId + ". Reason: " + result);
			}
			return null;
		});
	}

	public CompletableFuture<Void> togglePin(long messageId) {
		LOGGER.info("Task added - pining/unpining message {} in room {}.", messageId, roomId);
		return supplyAsync(() -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/owner-star").getAsString();
			LOGGER.debug("Message {} pined/unpined in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot pin/unpin message " + messageId + ". Reason: " + result);
			}
			return null;
		});
	}

	/**
	 * Causes the current logged user to leave the room.
	 * <p>Calling this method multiple times has no effect.
	 */
	public void leave() {
		if (hasLeft) return;
		LOGGER.debug("Leaving room {} on {}", roomId, host);
		post("http://chat." + host + "/chats/leave/" + roomId, "quiet", "true");
		close();
		hasLeft = true;
	}
	
	/**
	 * Retrieves the {@link Message} having the given id.
	 * @param messageId Id of the message to fetch.
	 * @return Message with the given id.
	 */
	public Message getMessage(long messageId) {
		try {
			String plainContent = httpClient.get("http://chat." + host + "/message/" + messageId, cookies, "fkey", fkey, "plain", "true").parse().body().html();
			String content = httpClient.get("http://chat." + host + "/message/" + messageId, cookies, "fkey", fkey, "plain", "false").parse().body().html();
			Document documentHistory = httpClient.get("http://chat." + host + "/messages/" + messageId + "/history", cookies, "fkey", fkey).parse();
			User user = getUser(Long.parseLong(documentHistory.select(".username > a").first().attr("href").split("/")[2]));
			boolean deleted = documentHistory.select(".message .content").stream().anyMatch(e -> e.getElementsByTag("b").html().equals("deleted"));
			return new Message(messageId, user, plainContent, content, deleted);
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		
	}
	
	/**
	 * Retrieves the {@link User} having the given id.
	 * @param messageId Id of the user to fetch.
	 * @return User with the given id.
	 */
	public User getUser(long userId) {
		Document document;
		try {
			document = httpClient.get("http://chat." + host + "/users/" + userId, cookies).parse();
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		String userName = document.getElementsByClass("user-status").html().replace('\u2666', ' ').replace("&nbsp;", "").trim();
		String title = document.getElementsByClass("reputation-score").attr("title");
		int reputation = title.isEmpty() ? 0 : Integer.parseInt(title);
		boolean moderator = document.getElementsByClass("user-status").html().contains("\u2666");
		boolean roomOwner = document.getElementsByClass("roomcard").stream().anyMatch(e -> e.id().equals("room-" + roomId));
		return new User(userId, userName, reputation, moderator, roomOwner);
	}

	/**
	 * Returns the id of this room. This id needs to be combined with the host
	 * of this room to reference uniquely this room, as there can be rooms with
	 * the same id across multiple hosts.
	 * @return Id of this room.
	 */
	public long getRoomId() {
		return roomId;
	}

	/**
	 * Returns the host of this room. Values are <code>stackoverflow.com</code>,
	 * <code>stackexchange.com</code> and <code>meta.stackexchange.com</code>
	 * 
	 * @return Host of this room.
	 */
	public String getHost() {
		return host;
	}

	void close() {
		shutdown(executor);
		shutdown(eventExecutor);
		try {
			websocketSession.close();
		} catch (IOException e) { }
	}
	
	private void shutdown(ExecutorService executor) {
		executor.shutdown();
		try {
			while (!executor.awaitTermination(5, TimeUnit.SECONDS));
		} catch (InterruptedException e) { }
	}

	public static void main(String[] args) {
		Properties properties = new Properties();
		try (FileReader reader = new FileReader(System.getProperty("user.home") + "/chat.properties")) {
			properties.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StackExchangeClient client = new StackExchangeClient(properties.getProperty("email"), properties.getProperty("password"));
		try {
			CountDownLatch countDownLatch = new CountDownLatch(1);
			Room room = client.joinRoom("stackoverflow.com", 111347);
			Message message = room.getMessage(30419591);
			System.out.println(message.getId());
			System.out.println(message.getContent());
			System.out.println(message.getPlainContent());
			System.out.println(message.isDeleted());
			System.out.println(message.getUser().getId());
			System.out.println(message.getUser().getName());
			System.out.println(message.getUser().getReputation());
			System.out.println(message.getUser().isModerator());
			System.out.println(message.getUser().isRoomOwner());
			
//			Room room2 = client.joinRoom("stackoverflow.com", 95290);
//			room.addEventListener(EventType.MESSAGE_POSTED, e -> {
//				if (e.getContent().equals("die")) {
//					countDownLatch.countDown();
//				}
//			});
//			room.addEventListener(EventType.MESSAGE_REPLY, e -> room.replyTo(e.getMessageId(), "Blob"));
//			room2.addEventListener(EventType.MESSAGE_REPLY, e -> room2.replyTo(e.getMessageId(), "Blob"));
//			try {
//				countDownLatch.await();
//			} catch (InterruptedException e1) {
//			}
			// CompletableFuture.allOf(room.send("TUNAKI ROCKS")).join();
		} finally {
			client.close();
		}
		// Room room2 = client.joinRoom("stackoverflow.com", 108192);
		// try {
		// CompletableFuture.allOf(IntStream.range(0, 20).mapToObj(i -> (i % 2
		// == 0 ? room :room2).send("Blob blob blob " +
		// i)).toArray(CompletableFuture[]::new)).join();
		// } finally {
		// client.close();
		// }
	}

}
