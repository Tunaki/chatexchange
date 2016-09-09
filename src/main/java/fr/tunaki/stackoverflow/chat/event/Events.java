package fr.tunaki.stackoverflow.chat.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.Room;

/**
 * Utility class to operate on events.
 * @author Tunaki
 */
public final class Events {

	private Events() { }

	/**
	 * Transforms the raw chat events to a list of event instances, with their corresponding data.
	 * @param events Raw chat events, as returned the the StackExchange chat websockets.
	 * @param room Room to return events from.
	 * @return List of events with their data.
	 */
	public static List<Event> fromJsonData(JsonArray events, Room room) {
		//kicked?
		if (events.size() == 2 && jsonObjects(events).anyMatch(o -> getEventType(o) == 4) && jsonObjects(events).anyMatch(o -> getEventType(o) == 15)) {
			return new ArrayList<>(Arrays.asList(new KickedEvent(events)));
		}
		return jsonObjects(events).filter(object -> object.get("room_id").getAsLong() == room.getRoomId())
				.map(object -> {
					switch (getEventType(object)) {
					case 1:
						if (object.get("user_id").getAsLong() > 0) {
							return new MessagePostedEvent(object, getMessage(room, object));
						}
						return null;
					case 2: return new MessageEditedEvent(object, getMessage(room, object));
					case 3: return new UserEnteredEvent(object);
					case 4: return new UserLeftEvent(object);
					case 6: return new MessageStarredEvent(object, getMessage(room, object));
					case 10: return new MessageDeletedEvent(object, getMessage(room, object));
					case 8: return new UserMentionedEvent(object, getMessage(room, object));
					case 18: return new MessageReplyEvent(object, getMessage(room, object));
					default:
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private static Stream<JsonObject> jsonObjects(JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsJsonObject);
	}

	private static Message getMessage(Room room, JsonObject object) {
		return room.getMessage(object.get("message_id").getAsLong());
	}

	private static int getEventType(JsonObject object) {
		return object.get("event_type").getAsInt();
	}

}
