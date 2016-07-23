package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Message;

/**
 * Holds the data for a mention message event.
 * @author Tunaki
 */
public class UserMentionedEvent extends PingMessageEvent {
	
	UserMentionedEvent(JsonElement jsonElement, Message message) {
		super(jsonElement, message);
	}

}
