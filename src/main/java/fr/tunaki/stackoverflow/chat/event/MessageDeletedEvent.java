package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Holds the data for a deleted message event.
 * @author Tunaki
 */
public class MessageDeletedEvent extends MessageEvent {

	MessageDeletedEvent(JsonElement jsonElement) {
		super(jsonElement);
	}

}
