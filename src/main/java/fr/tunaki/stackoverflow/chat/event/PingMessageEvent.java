package fr.tunaki.stackoverflow.chat.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents an event that is the result of ping message action. A ping corresponds to a message that either replies to another
 * message (with the <code>:{messageId}</code> chat feature) or mentions a user (with the <code>@{userName}</code> chat feature).
 * <p>It is a notification sent to the user, that will also appear in their global inbox, if not acknowleged before that. 
 * @author Tunaki
 */
public abstract class PingMessageEvent extends MessageEvent {
	
	private long targetUserId;
	private long parentMessageId;

	PingMessageEvent(JsonElement jsonElement) {
		super(jsonElement);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		targetUserId = jsonObject.get("target_user_id").getAsLong();
		parentMessageId = jsonObject.get("parent_id").getAsLong();
	}
	
	/**
	 * Returns the user id that was pinged by this event.
	 * @return Id of the user that was pinged by this event.
	 */
	public long getTargetUserId() {
		return targetUserId;
	}
	
	/**
	 * Returns the message id that this event's message is pinging. It will correspond to a message made by the targeted user.
	 * For replies, it corresponds to the message that was replied to; for mentions, it corresponds to the latest message of that user.
	 * @return Id of the message that this event's message is pinging.
	 */
	public long getParentMessageId() {
		return parentMessageId;
	}

}
