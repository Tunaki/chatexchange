package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Holds the data for an post message event.
 * @author Tunaki
 */
public class MessagePostedEvent extends MessageEvent {
	
	MessagePostedEvent(JsonElement jsonElement, Message message) {
		super(jsonElement, message);
	}

}
