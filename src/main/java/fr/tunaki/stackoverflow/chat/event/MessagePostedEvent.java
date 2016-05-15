package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Holds the data for an post message event.
 * @author Tunaki
 */
public class MessagePostedEvent extends MessageEvent {
	
	MessagePostedEvent(JsonElement jsonElement) {
		super(jsonElement);
	}

}
