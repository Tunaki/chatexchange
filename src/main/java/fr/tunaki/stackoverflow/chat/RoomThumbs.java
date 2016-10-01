package fr.tunaki.stackoverflow.chat;

import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates general information about a chat room, such as its id, name, description...
 * <p>The description of a chat room corresponds to the text displayed below its name. A chat room is also associated to a
 * list of tags.
 * @author Tunaki
 */
public final class RoomThumbs {

	private int id;
	private String name;
	private String description;
	private boolean favorite;
	private List<String> tags;

	RoomThumbs(int id, String name, String description, boolean favorite, List<String> tags) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.favorite = favorite;
		this.tags = tags;
	}

	/**
	 * Returns the id of this room.
	 * @return Id of the room.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of this room. This is also corresponds to the HTML title.
	 * @return Name of this room.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of this room. This is the text below the name of the room.
	 * @return Description of this room.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns whether this current logged-in user has favorited this room or not.
	 * @return Has the current logged-in user favorited this room?
	 */
	public boolean isFavorite() {
		return favorite;
	}

	/**
	 * Returns the list of tags associated with this room. Those appear below the description of the chat room.
	 * @return List of tags for this room.
	 */
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}

}
