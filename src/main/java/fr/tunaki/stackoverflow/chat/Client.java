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

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class Client {
	
	private static final Pattern OPEN_ID_PROVIDER_PATTERN = Pattern.compile("(https://openid.stackexchange.com/user/.*?)\"");
	
	private String openIdProvider;
	private Map<String, String> cookies = new HashMap<>();
	
	static {
		System.setProperty("http.agent", "Mozilla");
	}
	
	private List<ChatRoom> rooms = new ArrayList<>();
	
	public Client(String email, String password) {
		try {
			SEOpenIdLogin(email, password);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private void SEOpenIdLogin(String email, String password) throws IOException {
	    Response response = Jsoup.connect("https://openid.stackexchange.com/account/login").method(Method.GET).execute();
	    String fkey = response.parse().select("input[name='fkey']").val();
	    response = Jsoup.connect("https://openid.stackexchange.com/account/login/submit").data("email", email, "password", password, "fkey", fkey).method(Method.POST).cookies(response.cookies()).execute();
	    cookies.putAll(response.removeCookie("anon").cookies());
	    Matcher matcher = OPEN_ID_PROVIDER_PATTERN.matcher(response.parse().getElementById("delegate").html());
	    if (!matcher.find()) {
	    	throw new IllegalStateException("Cannot retrieve the Open ID provider");
	    }
	    openIdProvider = matcher.group(1);
	}
	
	public ChatRoom joinRoom(String host, long roomId) {
		if (rooms.stream().anyMatch(r -> r.getHost().equals(host) && r.getRoomId() == roomId)) {
			throw new RuntimeException("Cannot join a room you are already in");
		}
		if (rooms.stream().allMatch(r -> !r.getHost().equals(host))) {
			try {
				siteLogin(host);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		ChatRoom chatRoom = new ChatRoom(host, roomId, cookies);
		rooms.add(chatRoom);
		return chatRoom;
	}

	private void siteLogin(String host) throws IOException {
		Response response = Jsoup.connect("http://" + host + "/users/login?returnurl=" + URLEncoder.encode("http://" + host + "/", "UTF-8")).method(Method.GET).execute();
		cookies.putAll(response.cookies());
		String fkey = response.parse().select("input[name='fkey']").val();
		response = Jsoup.connect("http://" + host + "/users/authenticate").data("fkey", fkey, "openid_identifier", openIdProvider).method(Method.POST).cookies(cookies).execute();
		cookies.putAll(response.cookies());
		checkLoggedIn(host);
	}
	
	private void checkLoggedIn(String host) throws IOException {
        Response response = Jsoup.connect("http://" + host + "/users/current").method(Method.GET).cookies(cookies).execute();
        if (response.parse().getElementsByClass("reputation").first() == null) {
            throw new IllegalStateException("Unable to login to Stack Exchange.");
        }
    }
	
	public void close() {
		rooms.forEach(ChatRoom::close);
	}

}
