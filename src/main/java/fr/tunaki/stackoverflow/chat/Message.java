package fr.tunaki.stackoverflow.chat;

/**
 * This class represents a chat message.
 * <p>The content of the message is either plain or not. A plain content is the original markdown source of the message, whereas
 * a formatted content is the rendered HTML of the message. For example, a formatted cv-pls message will contain
 * an anchor (linking to the tag page for cv-pls) and the corresponding plain message will contain <code>[tag:cv-pls]</code> instead.
 * <p>A message is inherently linked to a chat room: an instance of {@link Message} can only be obtained by calling {@link Room#getMessage(long)},
 * by giving it the id of the message to look for. 
 * <p>A message also contains a reference to the user that posted it. Refer to {@link User}.
 * <p>When a message is deleted, its content and user will always be <code>null</code>, except if the current user is room-owner
 * or it is one of their own message.
 * @author Tunaki.
 */
public final class Message {
	
	private long id;
	private User user;
	private String plainContent;
	private String content;
	private boolean deleted;
	
	Message(long id, User user, String plainContent, String content, boolean deleted) {
		this.id = id;
		this.user = user;
		this.plainContent = plainContent;
		this.content = content;
		this.deleted = deleted;
	}

	/**
	 * Returns the id of this message.
	 * @return Id of this message.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the user that posted this message. This will be <code>null</code> if the current user is not a room-owner
	 * or this message is not one of their own message.
	 * @return User that posted this message.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns the plain content of this message. This is the original markdown source of the message.
	 * This will be <code>null</code> if the current user is not a room-owner or this message is not one of their own message.
	 * @return Plain content of this message
	 */
	public String getPlainContent() {
		return plainContent;
	}

	/**
	 * Returns the content of this message. This is the rendered HTML of the message.
	 * This will be <code>null</code> if the current user is not a room-owner or this message is not one of their own message.
	 * @return Content of this message
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Tells whether this message was deleted.
	 * @return Is this message deleted?
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
}
