package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Holds the data for a mention message event.
 * @author Tunaki
 */
public class UserMentionedEvent extends PingMessageEvent {
	
	UserMentionedEvent(JsonElement jsonElement) {
		super(jsonElement);
	}

}
