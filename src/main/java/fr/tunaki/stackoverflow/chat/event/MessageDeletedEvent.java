package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Holds the data for a deleted message event.
 * @author Tunaki
 */
public class MessageDeletedEvent extends MessageEvent {

	MessageDeletedEvent(JsonElement jsonElement, Message message) {
		super(jsonElement, message);
	}

}
