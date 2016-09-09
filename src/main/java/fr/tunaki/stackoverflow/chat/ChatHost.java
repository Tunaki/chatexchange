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

	private final String name;

	private ChatHost(String name) {
		this.name = name;
	}

	String getName() {
		return name;
	}

}
