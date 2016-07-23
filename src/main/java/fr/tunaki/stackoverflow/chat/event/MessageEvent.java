package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Represents an event that is the result of an action being performed on a message. This is the base class for all messages type
 * events, like posting, editing, replying, etc.
 * <p>The content of the message sent by the chat event is HTML encoded: this class will unescape the HTML entities.
 * @author Tunaki
 */
public abstract class MessageEvent extends Event {
	
	private Message message;

//	private int editCount;
//	private int starCount;
//	private int pinCount;

	MessageEvent(JsonElement jsonElement, Message message) {
		super(jsonElement);
		this.message = message;
	}

	/**
	 * Returns the message that triggered this event. 
	 * @return Message.
	 */
	public Message getMessage() {
		return message;
	}

}
