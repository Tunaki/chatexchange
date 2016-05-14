package fr.tunaki.stackoverflow.chat.event;

enum StackExchangeEventType {
	
    MESSAGE_POSTED(1),
    MESSAGE_EDITED(2),
    USER_ENTERED(3),
    USER_LEFT(4),
    ROOM_NAME_CHANGED(5),
    MESSAGE_STARRED(6),
    DEBUG_MESSAGE(7),
    USER_MENTIONED(8),
    MESSAGE_FLAGGED(9),
    MESSAGE_DELETED(10),
    FILE_ADDED(11),
    MODERATOR_FLAG(12),
    USER_SETTINGS_CHANGED(13),
    GLOBAL_NOTIFICATION(14),
    ACCESS_LEVEL_CHANGED(15),
    USER_NOTIFICATION(16),
    INVITATION(17),
    MESSAGE_REPLY(18),
    MESSAGE_MOVED_OUT(19),
    MESSAGE_MOVED_IN(20),
    TIME_BREAK(21),
    FEED_TICKER(22),
    USER_SUSPENDED(29),
    USER_MERGED(30);

	private final int code;

	private StackExchangeEventType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

}
