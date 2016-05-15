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
	private int starCount;
	private int pinCount;

	MessageEvent(JsonElement jsonElement) {
		super(jsonElement);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		content = orDefault(jsonObject.get("content"), null, e -> Jsoup.clean(e.getAsString(), Whitelist.relaxed()));
		messageId = jsonObject.get("message_id").getAsLong();
		editCount = orDefault(jsonObject.get("message_edits"), 0, JsonElement::getAsInt);
		starCount = orDefault(jsonObject.get("message_stars"), 0, JsonElement::getAsInt);
		pinCount = orDefault(jsonObject.get("message_owner_stars"), 0, JsonElement::getAsInt);
	}

	/**
	 * Returns the content of the message. This can be <code>null</code> (for example in the case of a deleted message event). 
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

	/**
	 * Returns how many times the message was starred.
	 * @return Number of times the message was starred.
	 */
	public int getStarCount() {
		return starCount;
	}

	/**
	 * Returns how many times the message was pinned.
	 * @return Number of times the message was pinned.
	 */
	public int getPinCount() {
		return pinCount;
	}
	
}
