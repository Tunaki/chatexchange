package fr.tunaki.stackoverflow.chat.event;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a chat event.
 * <p>An event is an action that happened in a room and contains a message. This class is final and cannot be instantiated, only
 * the pre-defined constants must be used.
 * @author Tunaki
 * @param <T> Type of the message for this event.
 */
public final class EventType<T> {
	
	private static final Map<Class<Object>, EventType<Object>> LOOKUP = new HashMap<>(); 
	
	/**
	 * Event raised when a message is posted in a room.
	 * This event only targets messages posted by users, and not system-generated messages (like adding a feed).
	 * <p>All messages posted by users raise this event, even replies or mentions.
	 */
	public static final EventType<MessagePostedEvent> MESSAGE_POSTED = new EventType<>(MessagePostedEvent.class);
	
	/**
	 * Event raised when a message is edited in a room.
	 * <p>All messages posted by users and then edited raise this event, even replies or mentions.
	 */
	public static final EventType<MessageEditedEvent> MESSAGE_EDITED = new EventType<>(MessageEditedEvent.class);
	
	/**
	 * Event raised when a reply is posted to the current logged-in user. A reply is a message targeting a specific other message.
	 * In chat, this is the <code>:{messageId}</code> feature.
	 * <p>When this event is raised, a corresponding {@link #MESSAGE_POSTED} or {@link #MESSAGE_EDITED} will be raised.
	 * This event is still useful to listen specifically to replies of one's messages instead of all posted / edited messages.
	 */
	public static final EventType<MessageReplyEvent> MESSAGE_REPLY = new EventType<>(MessageReplyEvent.class);
	
	/**
	 * Event raised when a mention of the current logged-in user is made. A mention is a message pinging a user without replying
	 * to a specific message. In chat, this is the <code>@{username}</code> feature.
	 * <p>When this event is raised, a corresponding {@link #MESSAGE_POSTED} or {@link #MESSAGE_EDITED} will be raised.
	 * This event is still useful to listen specifically to mentions of the logged-in user instead of all posted / edited messages.
	 */
	public static final EventType<UserMentionedEvent> USER_MENTIONED = new EventType<>(UserMentionedEvent.class);
	
	/**
	 * Event raised when a user is entering the chat room. This event is only raised when the user wasn't previously in the room, 
	 * meaning that they previously left it or never entered.
	 */
	public static final EventType<UserEnteredEvent> USER_ENTERED = new EventType<>(UserEnteredEvent.class);
	
	/**
	 * Event raised when a user is leaving the chat room, either as a result of inactivity or because they clicked the "leave" link.
	 */
	public static final EventType<UserLeftEvent> USER_LEFT = new EventType<>(UserLeftEvent.class);
	
	/**
	 * Event raised when a message is starred, unstarred, pinned or unpinned.
	 */
	public static final EventType<MessageStarredEvent> MESSAGE_STARRED = new EventType<>(MessageStarredEvent.class);
	
	/**
	 * Event raised when a message is deleted.
	 */
	public static final EventType<MessageDeletedEvent> MESSAGE_DELETED = new EventType<>(MessageDeletedEvent.class);
	
	/**
	 * Event raised when a user was kicked out of the chat room.
	 */
	public static final EventType<KickedEvent> KICKED = new EventType<>(KickedEvent.class);
	
	@SuppressWarnings("unchecked")
	private EventType(Class<T> clazz) {
		LOOKUP.put((Class<Object>) clazz, (EventType<Object>) this);
	}
	
	public static EventType<Object> fromEvent(Event event) {
		return LOOKUP.get(event.getClass());
	}
	
}
