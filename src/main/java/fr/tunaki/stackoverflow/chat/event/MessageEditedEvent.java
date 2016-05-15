package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Holds the data for an edit message event.
 * @author Tunaki
 */
public class MessageEditedEvent extends MessageEvent {
	
	MessageEditedEvent(JsonElement jsonElement) {
		super(jsonElement);
	}

}
