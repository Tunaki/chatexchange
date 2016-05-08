package fr.tunaki.stackoverflow.chat;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class ChatRoom {
	
	private static final String SUCCESS = "ok";

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	private long roomId;
	private String host;
	private String fkey;
	
	private Map<String, String> cookies = new HashMap<>();
	
	ChatRoom(String host, long roomId, Map<String, String> cookies) {
		this.roomId = roomId;
		this.host = host;
		this.cookies.putAll(cookies);
		fkey = retrieveFKey(roomId);
		executor.scheduleAtFixedRate(() -> fkey = retrieveFKey(roomId), 0, 1, TimeUnit.HOURS);
	}
	
	private Connection connectBase(String url) {
		return Jsoup.connect(url).cookies(cookies).ignoreContentType(true);
	}
	
	private Response get(String url) throws IOException {
		return connectBase(url).method(Method.GET).execute();
	}
	
	private JsonElement post(String url, String... data) {
		Response response;
		try {
			response = connectBase(url).method(Method.POST).data(data).data("fkey", fkey).execute();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return new JsonParser().parse(response.body());
	}
	
	private String retrieveFKey(long roomId) {
		try {
			Response response = get("http://chat.stackoverflow.com/rooms/" + roomId);
			cookies.putAll(response.cookies());
			return response.parse().getElementById("fkey").val();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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
		Client client = new Client(properties.getProperty("email"), properties.getProperty("password"));
		ChatRoom room = client.joinRoom("stackoverflow.com", 108192);
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
		try { Thread.sleep(5000); } catch (InterruptedException e) { }
	}

}
