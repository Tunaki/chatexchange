package fr.tunaki.stackoverflow.chat;

/**
 * Base exception for representing an exception during a processing of a chat operation.
 * @author Tunaki
 */
public class ChatOperationException extends RuntimeException {

	private static final long serialVersionUID = 6497584841951065261L;

	public ChatOperationException(String message) {
		super(message);
	}

	public ChatOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChatOperationException(Throwable cause) {
		super(cause);
	}

}
