package fr.tunaki.stackoverflow.chat;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class Room {

	private static final String SUCCESS = "ok";

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private long roomId;
	private String host;
	private String fkey;
	private HttpClient httpClient;

	Room(String host, long roomId, HttpClient httpClient) {
		this.roomId = roomId;
		this.host = host;
		this.httpClient = httpClient;
		fkey = retrieveFKey(roomId);
		executor.scheduleAtFixedRate(() -> fkey = retrieveFKey(roomId), 0, 1, TimeUnit.HOURS);
	}

	private JsonElement post(String url, String... data) {
		Response response;
		try {
			String[] dataWithFKey = new String[data.length + 2];
			dataWithFKey[0] = "fkey";
			dataWithFKey[1] = fkey;
			System.arraycopy(data, 0, dataWithFKey, 2, data.length);
			response = httpClient.post(url, dataWithFKey);
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
		return new JsonParser().parse(response.body());
	}

	private String retrieveFKey(long roomId) {
		try {
			Response response = httpClient.get("http://chat.stackoverflow.com/rooms/" + roomId);
			return response.parse().getElementById("fkey").val();
		} catch (IOException e) {
			throw new ChatOperationException(e);
		}
	}

	public long send(String message) {
		return post("http://chat." + host + "/chats/" + roomId + "/messages/new", "text", message).getAsJsonObject().get("id").getAsLong();
	}

	public boolean edit(long messageId, String message) {
		return SUCCESS.equals(post("http://chat." + host + "/messages/" + messageId, "text", message).getAsString());
	}

	public boolean delete(long messageId) {
		return SUCCESS.equals(post("http://chat." + host + "/messages/" + messageId + "/delete").getAsString());
	}

	public boolean toggleStar(long messageId) {
		return SUCCESS.equals(post("http://chat." + host + "/messages/" + messageId + "/star").getAsString());
	}

	public boolean togglePin(long messageId) {
		return SUCCESS.equals(post("http://chat." + host + "/messages/" + messageId + "/owner-star").getAsString());
	}

	public long getRoomId() {
		return roomId;
	}

	public String getHost() {
		return host;
	}

	void close() {
		executor.shutdownNow();
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
		Room room = client.joinRoom("stackoverflow.com", 108192);
		long messageId = room.send("Blob blob blob");
		throttle();
		room.edit(messageId, "Plop plop plop");
		throttle();
		room.delete(messageId);
		messageId = 30404221;
		throttle();
		System.out.println(room.toggleStar(messageId));
		throttle();
		System.out.println(room.toggleStar(messageId));
		throttle();
		System.out.println(room.togglePin(messageId));
		throttle();
		System.out.println(room.togglePin(messageId));
		throttle();
		client.close();
	}

	private static void throttle() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

}
