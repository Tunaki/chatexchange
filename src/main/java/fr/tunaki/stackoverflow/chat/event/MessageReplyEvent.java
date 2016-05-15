package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Holds the data for an reply message event.
 * @author Tunaki
 */
public class MessageReplyEvent extends PingMessageEvent {
	
	MessageReplyEvent(JsonElement jsonElement) {
		super(jsonElement);
	}
	
}
