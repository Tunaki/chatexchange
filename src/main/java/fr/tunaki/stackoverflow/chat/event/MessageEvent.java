package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 * Represents an event that is the result of an action being performed on a message. This is the base class for all messages type
 * events, like posting, editing, replying, etc.
 * <p>The content of the message sent by the chat event is HTML encoded: this class will unescape the HTML entities.
 * @author Tunaki
 */
public abstract class MessageEvent extends Event {

	private Message message;

	MessageEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		message = getRoom().getMessage(jsonObject.get("message_id").getAsLong());
	}

	/**
	 * Returns the message that triggered this event, at the time the event was raised.
	 * <p>The returned message will not be updated with regards to, e.g, stars or edits that were made after this event.
	 * If an updated message is needed, refer to {@link Room#getMessage(long)}.
	 * @return Message.
	 */
	public Message getMessage() {
		return message;
	}

}
