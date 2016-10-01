package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Holds the data for an reply message event.
 * @author Tunaki
 */
public class MessageReplyEvent extends PingMessageEvent {

	MessageReplyEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
	}

}
