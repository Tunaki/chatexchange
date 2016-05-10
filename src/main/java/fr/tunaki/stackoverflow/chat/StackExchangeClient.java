package fr.tunaki.stackoverflow.chat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;

public class StackExchangeClient {

	private static final Pattern OPEN_ID_PROVIDER_PATTERN = Pattern.compile("(https://openid.stackexchange.com/user/.*?)\"");

	private String openIdProvider;
	private HttpClient httpClient;

	private List<Room> rooms = new ArrayList<>();

	public StackExchangeClient(String email, String password, HttpClient httpClient) {
		this.httpClient = httpClient;
		try {
			SEOpenIdLogin(email, password);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void SEOpenIdLogin(String email, String password) throws IOException {
		Response response = httpClient.get("https://openid.stackexchange.com/account/login");
		String fkey = response.parse().select("input[name='fkey']").val();
		response = httpClient.post("https://openid.stackexchange.com/account/login/submit", "email", email, "password", password, "fkey", fkey);
		Matcher matcher = OPEN_ID_PROVIDER_PATTERN.matcher(response.parse().getElementById("delegate").html());
		if (!matcher.find()) {
			throw new IllegalStateException("Cannot retrieve the Open ID provider");
		}
		openIdProvider = matcher.group(1);
	}

	public Room joinRoom(String host, long roomId) {
		if (rooms.stream().anyMatch(r -> r.getHost().equals(host) && r.getRoomId() == roomId)) {
			throw new ChatOperationException("Cannot join a room you are already in.");
		}
		if (rooms.stream().allMatch(r -> !r.getHost().equals(host))) {
			try {
				siteLogin(host);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		Room chatRoom = new Room(host, roomId, httpClient);
		rooms.add(chatRoom);
		return chatRoom;
	}

	private void siteLogin(String host) throws IOException {
		Response response = httpClient.get("http://" + host + "/users/login?returnurl=" + URLEncoder.encode("http://" + host + "/", "UTF-8"));
		String fkey = response.parse().select("input[name='fkey']").val();
		response = httpClient.post("http://" + host + "/users/authenticate", "fkey", fkey, "openid_identifier", openIdProvider);
		checkLoggedIn(host);
	}

	private void checkLoggedIn(String host) throws IOException {
		Response response = httpClient.get("http://" + host + "/users/current");
		if (response.parse().getElementsByClass("reputation").first() == null) {
			throw new IllegalStateException("Unable to login to Stack Exchange.");
		}
	}

	public void close() {
		rooms.forEach(Room::close);
	}

}
