package fr.tunaki.stackoverflow.chat.event;

import java.time.Instant;

/**
 * Represents a user specific event, that is an event raised with the result of a user action.
 * @author Tunaki
 */
public interface UserEvent {
	
	/**
	 * Returns the instant in time (UTC) at which this event occured.
	 * @return Instant in time (UTC) at which this event occured.
	 */
	public Instant getInstant();

	/**
	 * Returns the content of the message.
	 * @return Content of the message.
	 */
	public String getContent();

	/**
	 * Returns the id of the user that raised this event.
	 * @return Id of the user that raised this event.
	 */
	public long getUserId();

	/**
	 * Returns the id of the user targeted by this event.
	 * @return Id of the user targeted by this event.
	 */
	public long getTargetUserId();

	/**
	 * Returns the display name of the user that raised this event.
	 * @return Display name of the user that raised this event.
	 */
	public String getUserName();

	/**
	 * Returns the id of the message that raised this event.
	 * @return Id of the message that raised this event.
	 */
	long getMessageId();

	/**
	 * Returns the id of the message targeted by this event. In case of a reply or a mention, this corresponds to the replied to
	 * or mentioned message.
	 * @return Id of the message targeted by this event.
	 */
	long getParentMessageId();

	/**
	 * Returns how many times the message was edited.
	 * @return Number of times the message was edited.
	 */
	int getEditCount();

	
}
