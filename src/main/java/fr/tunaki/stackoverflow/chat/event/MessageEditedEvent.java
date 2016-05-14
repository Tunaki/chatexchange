package fr.tunaki.stackoverflow.chat.event;

import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Holds the data for an edit message event.
 * @author Tunaki
 */
public class MessageEditedEvent {
	
	private Instant instant;
	private String content;
	private long userId;
	private String userName;
	private long messageId;
	private int editCount;

	MessageEditedEvent(JsonElement jsonElement) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		this.instant = Instant.ofEpochSecond(jsonObject.get("time_stamp").getAsLong());
		this.content = Jsoup.clean(jsonObject.get("content").getAsString(), Whitelist.relaxed());
		this.userId = jsonObject.get("user_id").getAsLong();
		this.userName = jsonObject.get("user_name").getAsString();
		this.messageId = jsonObject.get("message_id").getAsLong();
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
	 * Returns the id of the user that edited the message.
	 * @return Id of the user that edited the message.
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Returns the display name of the user that edited the message.
	 * @return Display name of the user that edited the message.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the id of the edited message.
	 * @return Id of the edited message.
	 */
	public long getMessageId() {
		return messageId;
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
		return "MessageEditedEvent [instant=" + instant + ", content=" + content + ", userId=" + userId + ", userName=" + userName + ", messageId=" + messageId + ", editCount=" + editCount + "]";
	}

}
