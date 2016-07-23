package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Holds the data for an reply message event.
 * @author Tunaki
 */
public class MessageReplyEvent extends PingMessageEvent {
	
	MessageReplyEvent(JsonElement jsonElement, Message message) {
		super(jsonElement, message);
	}
	
}
