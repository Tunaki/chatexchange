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
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.tunaki.stackoverflow.chat.event.Event;
import fr.tunaki.stackoverflow.chat.event.Events;

public final class Room {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);

	private static final String SUCCESS = "ok";
	private static final int THROTTLE_MS = 10000;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Executor messageEventExecutor = new Executor() {

		private final Semaphore semaphore = new Semaphore(1);
		private long lastEndTime = 0;

		@Override
		public void execute(Runnable command) {
			boolean acquired = false;
			do {
				try {
					semaphore.acquire();
					acquired = true;
				} catch (InterruptedException e) { }
			} while (!acquired);
			long timeBetweenLastCall = System.currentTimeMillis() - lastEndTime;
			if (timeBetweenLastCall < THROTTLE_MS) {
				try {
					Thread.sleep(THROTTLE_MS - timeBetweenLastCall);
				} catch (InterruptedException e) { }
			}
			try {
				command.run();
			} finally {
				semaphore.release();
			}
			lastEndTime = System.currentTimeMillis();
		}

	};
	
	private Session websocketSession;
	private Map<Event<Object>, List<Consumer<Object>>> chatEventListeners = new HashMap<>();

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
		String[] dataWithFKey = new String[data.length + 2];
		dataWithFKey[0] = "fkey";
		dataWithFKey[1] = fkey;
		System.arraycopy(data, 0, dataWithFKey, 2, data.length);
		Response response;
		try {
			response = httpClient.post(url, cookies, dataWithFKey);
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		return new JsonParser().parse(response.body());
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
        websocketUrl = websocketUrl + "?l=" + time;
        LOGGER.debug("Connecting to chat websocket " + websocketUrl);
        try {
        	websocketSession = client.connectToServer(new Endpoint() {
			    @Override
			    public void onOpen(Session session, EndpointConfig config) {
			        session.addMessageHandler(new MessageHandler.Whole<String>() {
			        	@Override
			            public void onMessage(String message) {
			            	handleChatEvent(message);
			            }
			        });
			    }
			}, configBuilder.build(), new URI(websocketUrl));
		} catch (DeploymentException | IOException | URISyntaxException e) {
			throw new ChatOperationException("Cannot connect to chat websocket", e);
		}
	}
	
	private void handleChatEvent(String json) {
		LOGGER.debug("Received message: " + json);
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		jsonObject.entrySet().stream().filter(e -> e.getKey().equals("r" + roomId)).map(Map.Entry::getValue).map(JsonElement::getAsJsonObject).map(o -> o.get("e")).map(JsonElement::getAsJsonArray).findFirst().ifPresent(events -> { 
			for (Event<?> event : Events.fromJsonData(events, roomId)) {
				for (Consumer<Object> listener : chatEventListeners.getOrDefault(event, Collections.emptyList())) {
					listener.accept(event.message());
				}
			}
		});
	}
	
	/**
	 * Adds a listener for the given event. Valid events are defined as constants of the {@link Event} class.
	 * <p>All listeners bound to a specific event will be called when the corresponding event is raised.
	 * @param event Event to listen to.
	 * @param listener Listener to add to this event.
	 */
	public <T> void addEventListener(Event<T> event, Consumer<T> listener) {
		@SuppressWarnings("unchecked") Event<Object> eventCast = (Event<Object>) event;
		@SuppressWarnings("unchecked") Consumer<Object> listenerCast = (Consumer<Object>) listener;
		chatEventListeners.computeIfAbsent(eventCast, e -> new ArrayList<>()).add(listenerCast);
	}

	/**
	 * Sends the given message asynchronously.
	 * @param message Content of the message to send.
	 * @return A future holding the id of the sent message.
	 */
	public CompletableFuture<Long> send(String message) {
		LOGGER.info("Task added - sending message '{}' to room {}.", message, roomId);
		Supplier<Long> supplier = () -> {
			JsonElement element = post("http://chat." + host + "/chats/" + roomId + "/messages/new", "text", message);
			LOGGER.debug("Message '{}' sent to room {}, raw result: {}", message, roomId, element);
			return element.getAsJsonObject().get("id").getAsLong();
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
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
	 * @return A future holding no value.
	 */
	public CompletableFuture<Void> edit(long messageId, String message) {
		LOGGER.info("Task added - editing message {} in room {}.", messageId, roomId);
		Supplier<Void> supplier = () -> {
			String result = post("http://chat." + host + "/messages/" + messageId, "text", message).getAsString();
			LOGGER.debug("Message {} edited to '{}' in room {}, raw result: {}", messageId, message, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot edit message " + messageId + ". Reason: " + result);
			}
			return null;
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
	}

	/**
	 * Deletes asynchronously the message having the given id.
	 * @param messageId Id of the message to delete.
	 * @return A future holding no value.
	 */
	public CompletableFuture<Void> delete(long messageId) {
		LOGGER.info("Task added - deleting message {} in room {}.", messageId, roomId);
		Supplier<Void> supplier = () -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/delete").getAsString();
			LOGGER.debug("Message {} deleted in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot delete message " + messageId + ". Reason: " + result);
			}
			return null;
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
	}

	public CompletableFuture<Void> toggleStar(long messageId) {
		LOGGER.info("Task added - starring/unstarring message {} in room {}.", messageId, roomId);
		Supplier<Void> supplier = () -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/star").getAsString();
			LOGGER.debug("Message {} starred/unstarred in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot star/unstar message " + messageId + ". Reason: " + result);
			}
			return null;
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
	}

	public CompletableFuture<Void> togglePin(long messageId) {
		LOGGER.info("Task added - pining/unpining message {} in room {}.", messageId, roomId);
		Supplier<Void> supplier = () -> {
			String result = post("http://chat." + host + "/messages/" + messageId + "/owner-star").getAsString();
			LOGGER.debug("Message {} pined/unpined in room {}, raw result: {}", messageId, roomId, result);
			if (!SUCCESS.equals(result)) {
				throw new ChatOperationException("Cannot pin/unpin message " + messageId + ". Reason: " + result);
			}
			return null;
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
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
	 * Returns the id of this room. This id needs to be combined with the host of this room to reference uniquely this room, 
	 * as there can be rooms with the same id across multiple hosts.
	 * @return Id of this room.
	 */
	public long getRoomId() {
		return roomId;
	}

	/**
	 * Returns the host of this room. Values are <code>stackoverflow.com</code>, <code>stackexchange.com</code> and <code>meta.stackexchange.com</code>
	 * @return Host of this room.
	 */
	public String getHost() {
		return host;
	}

	void close() {
		executor.shutdown();
		try {
			while (!executor.awaitTermination(5, TimeUnit.SECONDS));
		} catch (InterruptedException e) { }
		try {
			websocketSession.close();
		} catch (IOException e) { }
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
//			Room room2 = client.joinRoom("stackoverflow.com", 95290);
			room.addEventListener(Event.MESSAGE_POSTED, e -> {
				System.out.println(e);
				if (e.getContent().equals("die")) {
					countDownLatch.countDown();
				}
			});
			room.addEventListener(Event.MESSAGE_EDITED, System.out::println);
			room.addEventListener(Event.MESSAGE_REPLY, System.out::println);
			room.addEventListener(Event.USER_MENTIONED, System.out::println);
//			room2.addEventListener(Event.MESSAGE_POSTED, e -> {
//				room2.replyTo(e.getMessageId(), "blob");
//				System.out.println(e);
//				countDownLatch.countDown();
//			});
			try {
				countDownLatch.await();
			} catch (InterruptedException e1) { }
//			CompletableFuture.allOf(room.send("TUNAKI ROCKS")).join();
		} finally {
			client.close();
		}
//		Room room2 = client.joinRoom("stackoverflow.com", 108192);
//		try {
//			CompletableFuture.allOf(IntStream.range(0, 20).mapToObj(i -> (i % 2 == 0 ? room :room2).send("Blob blob blob " + i)).toArray(CompletableFuture[]::new)).join();
//		} finally {
//			client.close();
//		}
	}

}
