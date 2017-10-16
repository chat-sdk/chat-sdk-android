package co.chatsdk.core.events;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public enum EventType {

    ThreadAdded,
    ThreadRemoved,
    FollowerAdded,
    FollowerRemoved,
    FollowingAdded,
    FollowingRemoved,
    ThreadDetailsUpdated,
    MessageAdded,
    ThreadUsersChanged,
    UserMetaUpdated,
    UserPresenceUpdated,
    ContactAdded,
    ContactDeleted,
    ContactChanged,
    ContactsUpdated,
    TypingStateChanged,
    Logout,
    ThreadRead,
    ThreadReadReceiptUpdated,
}
