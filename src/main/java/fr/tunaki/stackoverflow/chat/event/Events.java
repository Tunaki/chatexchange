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
	public static List<Event> fromJsonData(JsonArray events, long roomId) {
		//TODO: special trickery for 2 event types meaning a single real event, like kicking or adding as RO
		return StreamSupport.stream(events.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.filter(object -> object.get("room_id").getAsLong() == roomId)
				.map(object -> {
					int eventType = object.get("event_type").getAsInt();
					switch (eventType) {
					case 1:
						if (object.get("user_id").getAsLong() > 0) {
							return new MessagePostedEvent(object);
						}
						return null;
					case 2: return new MessageEditedEvent(object);
					case 3: return new UserEnteredEvent(object);
					case 4: return new UserLeftEvent(object);
					case 6: return new MessageStarredEvent(object);
					case 10: return new MessageDeletedEvent(object);
					case 8: return new UserMentionedEvent(object);
					case 18: return new MessageReplyEvent(object);
					default:
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
