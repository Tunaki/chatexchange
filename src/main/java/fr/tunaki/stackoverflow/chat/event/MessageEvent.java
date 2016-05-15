package fr.tunaki.stackoverflow.chat.event;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents an event that is the result of an action being performed on a message. This is the base class for all messages type
 * events, like posting, editing, replying, etc.
 * <p>The content of the message will be stripped of some HTML tags, as defined by {@link Whitelist#relaxed()}, and HTML decoded.
 * @author Tunaki
 */
public abstract class MessageEvent extends Event {
	
	private String content;
	private long messageId;
	private int editCount;

	MessageEvent(JsonElement jsonElement) {
		super(jsonElement);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		content = Jsoup.clean(jsonObject.get("content").getAsString(), Whitelist.relaxed());
		messageId = jsonObject.get("message_id").getAsLong();
		editCount = jsonObject.has("message_edits") ? jsonObject.get("message_edits").getAsInt() : 0;
	}

	/**
	 * Returns the content of the message.
	 * @return Content of the message.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Returns the id of the message that raised this event.
	 * @return Id of the message that raised this event.
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

	
}
