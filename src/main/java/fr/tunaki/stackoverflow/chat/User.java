package fr.tunaki.stackoverflow.chat;

import java.time.Instant;

/**
 * This class represents a chat user. Users having a negative id are system users (like Feeds).
 * <p>A user is inherently linked to a chat room: an instance of {@link User} can only be obtained by calling {@link Room#getUser(long)},
 * or by retrieving the user of a message with {@link Message#getUser()}}. The same Stack Exchange user can have different
 * properties depending on the chat room it was obtained with, like being a room owner or not.
 * @author Tunaki
 */
public final class User {
	
	private long id;
	private String name;
	private int reputation;
	private boolean moderator;
	private boolean roomOwner;
	private Instant lastSeenDate;
	private Instant lastMessageDate;
	private boolean currentlyInRoom;
	
	User(long id, String name, int reputation, boolean moderator, boolean roomOwner, Instant lastSeenDate, Instant lastMessageDate, boolean currentlyInRoom) {
		this.id = id;
		this.name = name;
		this.reputation = reputation;
		this.moderator = moderator;
		this.roomOwner = roomOwner;
		this.lastSeenDate = lastSeenDate;
		this.lastMessageDate = lastMessageDate;
		this.currentlyInRoom = currentlyInRoom;
	}

	/**
	 * Returns the id of this user. This can be negative in case of system users.
	 * @return Id of this user.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the display name of this user.
	 * @return Display name of this user.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the amount of reputation this user has. In case of system users, this will be 0.
	 * @return Amount of reputation this user has.
	 */
	public int getReputation() {
		return reputation;
	}

	/**
	 * Tells whether this user is a moderator.
	 * @return Is this user a moderator?
	 */
	public boolean isModerator() {
		return moderator;
	}

	/**
	 * Tells whether this user is a room owner.
	 * @return Is this user a room owner?
	 */
	public boolean isRoomOwner() {
		return roomOwner;
	}

	/**
	 * Returns the last date at which this user was seen in the room (UTC). This will be <code>null</code> in the case of
	 * users that never joined the room.
	 * @return Last date at which this user was seen in the room (UTC).
	 */
	public Instant getLastSeenDate() {
		return lastSeenDate;
	}

	/**
	 * Returns the last date at which this user posted a message in the room (UTC). This will be <code>null</code> in the case of
	 * users that never joined the room.
	 * @return Last date at which this user posted a message in the room (UTC).
	 */
	public Instant getLastMessageDate() {
		return lastMessageDate;
	}

	/**
	 * Tells whether this user is currently in this room or not.
	 * @return Currently in the room or not.
	 */
	public boolean isCurrentlyInRoom() {
		return currentlyInRoom;
	}

}
