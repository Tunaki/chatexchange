package fr.tunaki.stackoverflow.chat;

/**
 * This class represents a valid chat host on the Stack Exchange network. There are 3 chat servers, which are <code>stackoverflow.com</code>,
 * <code>stackexchange.com</code> and <code>meta.stackexchange.com</code>.
 * @author Tunaki
 */
public enum ChatHost {

	STACK_OVERFLOW("stackoverflow.com"),
	STACK_EXCHANGE("stackexchange.com"),
	META_STACK_EXCHANGE("meta.stackexchange.com");

	private final String name, baseUrl;

	private ChatHost(String name) {
		this.name = name;
		this.baseUrl = "https://chat." + name;
	}

	/**
	 * @return The name of the chat server this host represents (example: <code>stackoverflow.com</code>).
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The base URL that points to this chat server (example: <code>https://chat.stackoverflow.com</code>).
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

}
