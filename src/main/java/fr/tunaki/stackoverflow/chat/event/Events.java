package fr.tunaki.stackoverflow.chat.event;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Utility class to operate on events.
 * @author Tunaki
 */
public final class Events {
	
	private Events() { }
	
	/**
	 * Transforms the raw chat events to a list of event instances, with their corresponding data.
	 * @param events Raw chat events, as returned the the StackExchange chat websockets.
	 * @param roomId Id of the room to return events from.
	 * @return List of events with their data.
	 */
	public static List<EventType<?>> fromJsonData(JsonArray events, long roomId) {
		//TODO: special trickery for 2 event types meaning a single real event, like kicking or adding as RO
		return StreamSupport.stream(events.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.filter(object -> object.get("room_id").getAsLong() == roomId)
				.map(object -> {
					int eventType = object.get("event_type").getAsInt();
					switch (eventType) {
					case 1:
						if (object.get("user_id").getAsLong() > 0) {
							return EventType.MESSAGE_POSTED.withData(object);
						}
						return null;
					case 2: return EventType.MESSAGE_EDITED.withData(object);
					case 3: return EventType.USER_ENTERED.withData(object);
					case 4: return EventType.USER_LEFT.withData(object);
					case 6: return EventType.MESSAGE_STARRED.withData(object);
					case 10: return EventType.MESSAGE_DELETED.withData(object);
					case 8: return EventType.USER_MENTIONED.withData(object);
					case 18: return EventType.MESSAGE_REPLY.withData(object);
					default:
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
