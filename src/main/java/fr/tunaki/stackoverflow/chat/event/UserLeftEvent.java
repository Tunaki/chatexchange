package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

/**
 * Represents the event where a user left the chat room. Leaving a chat room automatically happens after some time of inactivity
 * or after explicitely clicking the "leave" link in a chat room.
 * @author Tunaki
 */
public class UserLeftEvent extends Event {
	
	UserLeftEvent(JsonElement jsonElement) {
		super(jsonElement);
	}

}
