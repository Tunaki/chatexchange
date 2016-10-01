package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Holds the data for a deleted message event.
 * @author Tunaki
 */
public class MessageDeletedEvent extends MessageEvent {

	MessageDeletedEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
	}

}
