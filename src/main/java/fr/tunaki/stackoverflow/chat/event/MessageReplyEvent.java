package fr.tunaki.stackoverflow.chat.event;

import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Holds the data for an reply message event.
 * @author Tunaki
 */
public class MessageReplyEvent {
	
	private Instant instant;
	private String content;
	private long userId;
	private long targetUserId;
	private String userName;
	private long messageId;
	private long parentMessageId;
	private int editCount;

	MessageReplyEvent(JsonElement jsonElement) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		this.instant = Instant.ofEpochSecond(jsonObject.get("time_stamp").getAsLong());
		this.content = Jsoup.clean(jsonObject.get("content").getAsString(), Whitelist.relaxed());
		this.userId = jsonObject.get("user_id").getAsLong();
		this.targetUserId = jsonObject.get("target_user_id").getAsLong();
		this.userName = jsonObject.get("user_name").getAsString();
		this.messageId = jsonObject.get("message_id").getAsLong();
		this.parentMessageId = jsonObject.get("parent_id").getAsLong();
		this.editCount = jsonObject.get("message_edits").getAsInt();
	}
	
	/**
	 * Returns the instant in time (UTC) at which this event occured.
	 * @return Instant in time (UTC) at which this event occured.
	 */
	public Instant getInstant() {
		return instant;
	}

	/**
	 * Returns the content of the message.
	 * @return Content of the message.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the id of the user that made the reply.
	 * @return Id of the user that made the reply.
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Returns the id of the user targeted by the reply.
	 * @return Id of the user targeted by the reply.
	 */
	public long getTargetUserId() {
		return targetUserId;
	}

	/**
	 * Returns the display name of the user that made the reply.
	 * @return Display name of the user that made the reply.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the id of the reply message.
	 * @return Id of the reply message.
	 */
	public long getMessageId() {
		return messageId;
	}

	/**
	 * Returns the id of the message replied to.
	 * @return Id of the message replied to.
	 */
	public long getParentMessageId() {
		return parentMessageId;
	}

	/**
	 * Returns how many times the message was edited.
	 * @return Number of times the message was edited.
	 */
	public int getEditCount() {
		return editCount;
	}

	@Override
	public String toString() {
		return "MessageReplyEvent [instant=" + instant + ", content=" + content + ", userId=" + userId + ", targetUserId=" + targetUserId + ", userName=" + userName + ", messageId=" + messageId + ", parentMessageId=" + parentMessageId + ", editCount" + editCount + "]";
	}
	
}
