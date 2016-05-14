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
public class MessagePostedEvent implements UserEvent {
	
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

	@Override
	public Instant getInstant() {
		return instant;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public long getMessageId() {
		return messageId;
	}

	@Override
	public long getTargetUserId() {
		return -1;
	}

	@Override
	public long getParentMessageId() {
		return -1;
	}

	@Override
	public int getEditCount() {
		return 1;
	}

	@Override
	public String toString() {
		return "MessagePostedEvent [instant=" + instant + ", content=" + content + ", userId=" + userId + ", userName=" + userName + ", messageId=" + messageId + "]";
	}
	
}
