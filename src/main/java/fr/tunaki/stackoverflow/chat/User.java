package fr.tunaki.stackoverflow.chat;

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
	
	User(long id, String name, int reputation, boolean moderator, boolean roomOwner) {
		this.id = id;
		this.name = name;
		this.reputation = reputation;
		this.moderator = moderator;
		this.roomOwner = roomOwner;
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

}
