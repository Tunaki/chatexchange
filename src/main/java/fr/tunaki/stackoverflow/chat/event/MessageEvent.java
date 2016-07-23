package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Represents an event that is the result of an action being performed on a message. This is the base class for all messages type
 * events, like posting, editing, replying, etc.
 * <p>The content of the message sent by the chat event is HTML encoded: this class will unescape the HTML entities.
 * @author Tunaki
 */
public abstract class MessageEvent extends Event {
	
	private Message message;

	//FIXME: find how to move this to Message
	private int editCount;
	private int starCount;
	private int pinCount;

	MessageEvent(JsonElement jsonElement, Message message) {
		super(jsonElement);
		this.message = message;
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		editCount = orDefault(jsonObject.get("message_edits"), 0, JsonElement::getAsInt);
		starCount = orDefault(jsonObject.get("message_stars"), 0, JsonElement::getAsInt);
		pinCount = orDefault(jsonObject.get("message_owner_stars"), 0, JsonElement::getAsInt);
	}

	/**
	 * Returns the message that triggered this event. 
	 * @return Message.
	 */
	public Message getMessage() {
		return message;
	}
	
	/**
	 * Returns how many times the message was edited.
	 * @return Number of times the message was edited.
	 */
	public int getEditCount() {
		return editCount;
	}

	/**
	 * Returns how many times the message was starred.
	 * @return Number of times the message was starred.
	 */
	public int getStarCount() {
		return starCount;
	}

	/**
	 * Returns how many times the message was pinned.
	 * @return Number of times the message was pinned.
	 */
	public int getPinCount() {
		return pinCount;
	}

}
