package fr.tunaki.stackoverflow.chat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client used to authenticate with Stack Exchange. To properly dispose of this client once created, it is expected to be
 * closed by invoking the {@link #close()} method.
 * @author Tunaki
 */
public class StackExchangeClient implements AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(StackExchangeClient.class);

	private static final Pattern OPEN_ID_PROVIDER_PATTERN = Pattern.compile("(https://openid.stackexchange.com/user/.*?)\"");

	private String openIdProvider;

	private HttpClient httpClient;
	private Map<String, String> cookies = new HashMap<>();

	private List<Room> rooms = new ArrayList<>();
	
	/**
	 * Hosts where the bot is currently logged in
	 * */
	private HashSet<String> loggedInHosts = new HashSet<>();
	
	/**
	 * The user's e-mail-address
	 * This needs to be stored in order to login to a site when joining a room.
	 * With OpenID, this was not necessary because we only had to login once while initializing `StackExchangeClient`
	 * */
	private String email = null;
	
	/**
	 * The user's password
	 * This needs to be stored in order to login to a site when joining a room.
	 * With OpenID, this was not necessary because we only had to login once while initializing `StackExchangeClient`
	 * */
	private String password = null;

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
	 * Logs in to s given site
	 * @param email The user's e-mail-address
	 * @param password The password
	 * @param The host of the main site (NOT the chat.*! Use ChatHost.getName())
	 * */
	private void seLogin(String email, String password, String host) throws IOException {
		//The login-form has a hidden field called "fkey" which needs to be sent along with the mail and password
		Response response = httpClient.get("https://"+host+"/users/login", cookies);
		String fkey = response.parse().select("input[name='fkey']").val();
		
		response = httpClient.post("https://"+host+"/users/login", cookies, "email", email, "password", password, "fkey", fkey);
		
		// check if login succeeded
		Response checkResponse = httpClient.get("https://"+host+"/users/current", cookies);
		if (checkResponse.parse().getElementsByClass("js-inbox-button").first() == null) {
			LOGGER.debug(checkResponse.parse().html());
			throw new IllegalStateException("Unable to login to Stack Exchange. (Site: " + host + ")");
		}
		
		//Remember that the user is logged in on that site
		this.loggedInHosts.add(host);
	} // seLogin

	/**
	 * The old login-flow with OpenID
	 * @deprecated in 1.2.0. See meta: https://meta.stackexchange.com/q/307647/347985
	 * */
	@Deprecated
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

	/**
	 * Joins the given room for the given chat host.
	 * <p>Trying to join a room in which you are already in results in a <code>ChatOperationException</code>.
	 * @param host Host of the chat room to join.
	 * @param roomId Id of the room to join.
	 * @return <code>Room</code> joined.
	 */
	public Room joinRoom(ChatHost host, int roomId) {
		String mainSiteHost = host.getName();
		
		if (!this.loggedInHosts.contains(mainSiteHost)) {
			//not logged in on that site yet
			try {
				this.seLogin(email, password, mainSiteHost);
			} catch (IOException e) {
				LOGGER.error("Unable to login on " + mainSiteHost + " for " + host.getBaseUrl(), e);
				throw new ChatOperationException("Login to " + mainSiteHost + " failed!");
			}
		}
		
		if (rooms.stream().anyMatch(r -> r.getHost().equals(host) && r.getRoomId() == roomId)) {
			throw new ChatOperationException("Cannot join a room you are already in.");
		}
		
		Room chatRoom = new Room(host, roomId, httpClient, cookies);
		rooms.add(chatRoom);
		return chatRoom;
	}

	/**
	 * @deprecated in 1.2.0: This is not required anymore, but maybe someone can re-implement the account creation in the new login-flow?
	 * */
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
	}

	/**
	 * Closes this client by making the logged-in user leave all the chat rooms they joined.
	 * <p>Multiple invocations of this method has no further effect.
	 */
	@Override
	public void close() {
		rooms.forEach(Room::leave);
	}

}
