package fr.tunaki.stackoverflow.chat;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Room {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Room.class);

	private static final String SUCCESS = "ok";
	private static final int THROTTLE_MS = 10000;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private final Executor messageEventExecutor = new Executor() {

		private final Semaphore semaphore = new Semaphore(1);

		@Override
		public void execute(Runnable command) {
			boolean acquired = false;
			do {
				try {
					semaphore.acquire();
					acquired = true;
				} catch (InterruptedException e) { }
			} while (!acquired);
			try {
				Thread.sleep(THROTTLE_MS);
			} catch (InterruptedException e) { }
			try {
				command.run();
			} finally {
				semaphore.release();
			}
		}

	};

	private long roomId;
	private String host;
	private String fkey;
	private HttpClient httpClient;

	Room(String host, long roomId, HttpClient httpClient) {
		this.roomId = roomId;
		this.host = host;
		this.httpClient = httpClient;
		fkey = retrieveFKey(roomId);
		executor.scheduleAtFixedRate(() -> fkey = retrieveFKey(roomId), 1, 1, TimeUnit.HOURS);
	}

	private JsonElement post(String url, String... data) {
		String[] dataWithFKey = new String[data.length + 2];
		dataWithFKey[0] = "fkey";
		dataWithFKey[1] = fkey;
		System.arraycopy(data, 0, dataWithFKey, 2, data.length);
		Response response;
		try {
			response = httpClient.post(url, dataWithFKey);
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		return new JsonParser().parse(response.body());
	}

	private String retrieveFKey(long roomId) {
		try {
			Response response = httpClient.get("http://chat.stackoverflow.com/rooms/" + roomId);
			String fkey = response.parse().getElementById("fkey").val();
			LOGGER.debug("New fkey retrieved for room {}", roomId);
			return fkey;
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
	}

	public CompletableFuture<Long> send(String message) {
		LOGGER.info("Task added - sending message '{}' to room {}.", message, roomId);
		Supplier<Long> supplier = () -> {
			JsonElement element = post("http://chat." + host + "/chats/" + roomId + "/messages/new", "text", message);
			LOGGER.debug("Message '{}' sent to room {}, raw result: {}", message, roomId, element);
			return element.getAsJsonObject().get("id").getAsLong();
		};
		return CompletableFuture.supplyAsync(supplier, messageEventExecutor);
	}

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

	public long getRoomId() {
		return roomId;
	}

	public String getHost() {
		return host;
	}

	void close() {
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
		HttpClient httpClient = new HttpClient();
		StackExchangeClient client = new StackExchangeClient(properties.getProperty("email"), properties.getProperty("password"), httpClient);
		Room room = client.joinRoom("stackoverflow.com", 111347);
		Room room2 = client.joinRoom("stackoverflow.com", 108192);
		try {
			CompletableFuture.allOf(IntStream.range(0, 20).mapToObj(i -> (i % 2 == 0 ? room :room2).send("Blob blob blob " + i)).toArray(CompletableFuture[]::new)).join();
		} finally {
			client.close();
		}
	}

}
