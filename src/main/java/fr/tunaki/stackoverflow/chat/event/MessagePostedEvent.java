package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Holds the data for an post message event.
 * @author Tunaki
 */
public class MessagePostedEvent extends MessageEvent {

	MessagePostedEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
	}

}
