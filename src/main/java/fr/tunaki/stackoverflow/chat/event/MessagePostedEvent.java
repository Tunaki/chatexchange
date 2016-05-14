package fr.tunaki.stackoverflow.chat.event;

import java.time.Instant;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Holds the data for an post message event.
 * @author Tunaki
 */
public class MessagePostedEvent {
	
	private Instant instant;
	private String content;
	private long userId;
	private String userName;
	private long messageId;

	MessagePostedEvent(JsonElement jsonElement) {
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		this.instant = Instant.ofEpochSecond(jsonObject.get("time_stamp").getAsLong());
		this.content = Jsoup.clean(jsonObject.get("content").getAsString(), Whitelist.relaxed());
		this.userId = jsonObject.get("user_id").getAsLong();
		this.userName = jsonObject.get("user_name").getAsString();
		this.messageId = jsonObject.get("message_id").getAsLong();
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
	 * Returns the id of the user that posted the message.
	 * @return Id of the user that posted the message.
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * Returns the display name of the user that posted the message.
	 * @return Display name of the user that posted the message.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the id of the posted message.
	 * @return Id of the posted message.
	 */
	public long getMessageId() {
		return messageId;
	}

	@Override
	public String toString() {
		return "MessagePostedEvent [instant=" + instant + ", content=" + content + ", userId=" + userId + ", userName=" + userName + ", messageId=" + messageId + "]";
	}
	
}
