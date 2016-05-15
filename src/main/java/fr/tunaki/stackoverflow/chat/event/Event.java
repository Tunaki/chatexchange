package fr.tunaki.stackoverflow.chat.event;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Base class for all events raised in chat.
 * <p>An event represents an action that was triggered by a user or a system action. Actions made by user typically corresponds
 * to posting messages, editing messages, etc. and actions made by the system typically corresponds to feeds added, change in 
 * access level, etc.
 * <p>All events have a instant at which they occured, represented by an {@link Instant} object (UTC). They also have the user that
 * triggered the action (ID and display name). For system actions, the ID will be negative.  
 * @author Tunaki
 */
public abstract class Event {
	
	private Instant instant;
	private long userId;
	private String userName;

	Event(JsonElement jsonElement) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		instant = Instant.ofEpochSecond(jsonObject.get("time_stamp").getAsLong());
		userId = jsonObject.get("user_id").getAsLong();
		userName = orDefault(jsonObject.get("user_name"), null, JsonElement::getAsString);
	}
	
	/**
	 * Returns the instant in time (UTC) at which this event occured.
	 * @return Instant in time (UTC) at which this event occured.
	 */
	public Instant getInstant() {
		return instant;
	}

	/**
	 * Returns the id of the user that raised this event. For system generated event, the id will be negative.
	 * @return Id of the user that raised this event.
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Returns the display name of the user that raised this event. This can be <code>null</code> under unreproducible conditions.
	 * @return Display name of the user that raised this event.
	 */
	public String getUserName() {
		return userName;
	}
	
	protected <T> T orDefault(JsonElement element, T defaultValue, Function<JsonElement, T> function) {
		return element == null ? defaultValue : function.apply(element);
	}
	
	protected int orDefault(JsonElement element, int defaultValue, ToIntFunction<JsonElement> function) {
		return element == null ? defaultValue : function.applyAsInt(element);
	}
	
	protected boolean orDefault(JsonElement element, boolean defaultValue) {
		return element == null ? defaultValue : element.getAsBoolean();
	}
	
}
