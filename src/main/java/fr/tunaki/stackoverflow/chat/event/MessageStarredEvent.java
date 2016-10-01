package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Room;

/**
 * Represents the event of starring / unstarring and pining / unpining a message. There is no distinction between starring and
 * unstarring (conversely, between pinning and unpinning).
 * @author Tunaki
 */
public class MessageStarredEvent extends MessageEvent {

	private boolean starred;
	private boolean pinned;

	MessageStarredEvent(JsonElement jsonElement, Room room) {
		super(jsonElement, room);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		starred = orDefault(jsonObject.get("message_starred"), false);
		pinned = orDefault(jsonObject.get("message_owner_starred"), false);
	}

	/**
	 * Returns whether this event was as a result of the current logged-in user starring a message.
	 * @return Whether this event was as a result of the current logged-in user starring a message.
	 */
	public boolean wasStarred() {
		return starred;
	}

	/**
	 * Returns whether this event was as a result of the current logged-in user pinning a message.
	 * @return Whether this event was as a result of the current logged-in user pinning a message.
	 */
	public boolean wasPinned() {
		return pinned;
	}

}
