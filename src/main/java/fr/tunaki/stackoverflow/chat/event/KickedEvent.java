package fr.tunaki.stackoverflow.chat.event;

import java.util.Optional;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents an event resulting from a kick happening in the current room.
 * @author Tunaki
 */
public class KickedEvent extends Event {
	
	private long kickeeId;
	
	KickedEvent(JsonElement jsonElement) {
		super(getMasterJsonObject(jsonElement));
		JsonObject other = getJsonObjectForType(jsonElement, 4).orElseThrow(AssertionError::new);
		kickeeId = other.get("user_id").getAsLong();
	}
	
	private static JsonObject getMasterJsonObject(JsonElement jsonElement) {
		return getJsonObjectForType(jsonElement, 15).orElseThrow(AssertionError::new);
	}
	
	private static Optional<JsonObject> getJsonObjectForType(JsonElement jsonElement, int type) {
		return StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false).map(JsonElement::getAsJsonObject).filter(e -> e.get("event_type").getAsInt() == type).findFirst();
	}

	/**
	 * Returns the id of the user that was kicked.
	 * @return Id of the user that was kicked.
	 */
	public long getKickeeId() {
		return kickeeId;
	}

}
