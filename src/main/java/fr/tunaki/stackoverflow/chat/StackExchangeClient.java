package fr.tunaki.stackoverflow.chat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackExchangeClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeClient.class);

	private static final Pattern OPEN_ID_PROVIDER_PATTERN = Pattern.compile("(https://openid.stackexchange.com/user/.*?)\"");

	private String openIdProvider;
	
	private HttpClient httpClient;
	private Map<String, String> cookies = new HashMap<>();

	private List<Room> rooms = new ArrayList<>();

	public StackExchangeClient(String email, String password) {
		httpClient = new HttpClient();
		try {
			SEOpenIdLogin(email, password);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void SEOpenIdLogin(String email, String password) throws IOException {
		Response response = httpClient.get("https://openid.stackexchange.com/account/login", cookies);
		String fkey = response.parse().select("input[name='fkey']").val();
		response = httpClient.post("https://openid.stackexchange.com/account/login/submit", cookies, "email", email, "password", password, "fkey", fkey);
		Document document = response.parse();
		if (document.getElementsByClass("error").size() > 0) {
			LOGGER.debug(document.html());
			throw new ChatOperationException("Invalid OpenID credentials");
		}
		Matcher matcher = OPEN_ID_PROVIDER_PATTERN.matcher(document.getElementById("delegate").html());
		if (!matcher.find()) {
			LOGGER.debug(document.html());
			throw new IllegalStateException("Cannot retrieve the OpenID provider");
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
		Room chatRoom = new Room(host, roomId, httpClient, cookies);
		rooms.add(chatRoom);
		return chatRoom;
	}

	private void siteLogin(String host) throws IOException {
		Response response = httpClient.get("http://" + host + "/users/login?returnurl=" + URLEncoder.encode("http://" + host + "/", "UTF-8"), cookies);
		String fkey = response.parse().select("input[name='fkey']").val();
		response = httpClient.post("http://" + host + "/users/authenticate", cookies, "fkey", fkey, "openid_identifier", openIdProvider);
		Response checkResponse = httpClient.get("http://" + host + "/users/current", cookies);
		if (checkResponse.parse().getElementsByClass("reputation").first() == null) {
			LOGGER.debug(response.parse().html());
			LOGGER.debug(checkResponse.parse().html());
			throw new IllegalStateException("Unable to login to Stack Exchange.");
		}
	}

	public void close() {
		rooms.forEach(Room::leave);
	}

}
