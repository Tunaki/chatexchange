package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Holds the data for an edit message event.
 * @author Tunaki
 */
public class MessageEditedEvent extends MessageEvent {

	MessageEditedEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
	}

}
