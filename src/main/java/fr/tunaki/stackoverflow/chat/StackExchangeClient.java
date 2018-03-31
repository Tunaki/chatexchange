package fr.tunaki.stackoverflow.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client used to authenticate with Stack Exchange. To properly dispose of this client once created, it is expected to be
 * closed by invoking the {@link #close()} method.
 * @author Tunaki
 */
public class StackExchangeClient implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeClient.class);

	private HttpClient httpClient;
	private Map<String, String> cookies = new HashMap<>();

	private List<Room> rooms = new ArrayList<>();

	private String email;
	private String password;

	/**
	 * Constructs the client with the provided credentials. Those will be the credentials used to send messages.
	 * @param email Email of the account to connect with.
	 * @param password Password of the account to connect with.
	 */
	public StackExchangeClient(String email, String password) {
		httpClient = new HttpClient();
		this.email = email;
		this.password = password;
	}

	/**
	 * Joins the given room for the given chat host.
	 * <p>Trying to join a room in which you are already in results in a <code>ChatOperationException</code>.
	 * @param host Host of the chat room to join.
	 * @param roomId Id of the room to join.
	 * @return <code>Room</code> joined.
	 */
	public Room joinRoom(ChatHost host, int roomId) {
		if (rooms.stream().anyMatch(r -> r.getHost().equals(host) && r.getRoomId() == roomId)) {
			throw new ChatOperationException("Cannot join a room you are already in.");
		}
		if (rooms.stream().noneMatch(r -> r.getHost().equals(host))) {
			try {
				seLogin(email, password, host.getName());
			} catch (IOException e) {
				throw new ChatOperationException("Login to " + host + " failed!", e);
			}
		}
		Room chatRoom = new Room(host, roomId, httpClient, cookies);
		rooms.add(chatRoom);
		return chatRoom;
	}

	private void seLogin(String email, String password, String host) throws IOException {
		Response response = httpClient.get("https://" + host + "/users/login", cookies);
		String fkey = response.parse().select("input[name='fkey']").val();

		response = httpClient.post("https://" + host + "/users/login", cookies, "email", email, "password", password, "fkey", fkey);

		// check logged in
		Response checkResponse = httpClient.get("https://" + host + "/users/current", cookies);
		if (checkResponse.parse().getElementsByClass("js-inbox-button").first() == null) {
			LOGGER.debug(response.parse().html());
			throw new IllegalStateException("Unable to login to Stack Exchange.");
		}
	}

	/* FIXME: figure out how to implement account creation
	@Deprecated
	private void siteLogin(String host) throws IOException {
		Response response = httpClient.get("https://" + host + "/users/login?returnurl=" + URLEncoder.encode("https://" + host + "/", "UTF-8"), cookies);
		String fkey = response.parse().select("input[name='fkey']").val();
		response = httpClient.post("https://" + host + "/users/authenticate", cookies, "fkey", fkey, "openid_identifier", openIdProvider);
		Document document = response.parse();

		// confirmation prompt?
		if (response.url().toString().startsWith("https://openid.stackexchange.com/account/prompt")) {
			LOGGER.trace("Confirmation prompt \n" + document.html());
			String session = document.select("input[name='session']").first().val();
			fkey = document.select("input[name='fkey']").first().val();
			Response promptResponse = httpClient.post("https://openid.stackexchange.com/account/prompt/submit", cookies, "session", session, "fkey", fkey);
			document = promptResponse.parse();
			LOGGER.trace("Confirmation prompt response \n" + document.html());
		}

		// when the account doesn't exist on this site, confirm its creation
		if (!document.select("form[action='/users/openidconfirm']").isEmpty()) {
			LOGGER.debug("Account doesn't exist on target site '{}', confirming new account", host);
			String session = document.select("input[name='s']").first().val();
			fkey = document.select("input[name='fkey']").first().val();
			Response newAccountResponse = httpClient.post("https://" + host + "/users/openidconfirm", cookies, "s", session, "fkey", fkey);
			LOGGER.trace("New account confirmation response \n" + newAccountResponse.parse().html());
		}

		// check logged in
		Response checkResponse = httpClient.get("https://" + host + "/users/current", cookies);
		if (checkResponse.parse().getElementsByClass("js-inbox-button").first() == null) {
			LOGGER.debug(response.parse().html());
			throw new IllegalStateException("Unable to login to Stack Exchange.");
		}
	}*/

	/**
	 * Closes this client by making the logged-in user leave all the chat rooms they joined.
	 * <p>Multiple invocations of this method has no further effect.
	 */
	@Override
	public void close() {
		rooms.forEach(Room::leave);
	}

}
