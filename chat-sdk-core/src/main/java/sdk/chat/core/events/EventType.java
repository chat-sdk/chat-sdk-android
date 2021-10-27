package sdk.chat.core.events;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public enum EventType {
    ThreadAdded,
    ThreadRemoved,
    ThreadMetaUpdated,
    MessageAdded,
    MessageUpdated,
    MessageRemoved,
    MessageSendStatusUpdated,
    MessageSendFailed,

    ThreadsUpdated,
    ThreadUserAdded,
    ThreadUserRemoved,
    ThreadUserUpdated,

    ThreadUserRoleUpdated,
    UserMetaUpdated,
    UserPresenceUpdated,
    ContactAdded,
    ContactDeleted,
    ContactsUpdated,
    TypingStateUpdated,
    Logout,
    ThreadRead,
    MessageReadReceiptUpdated,
    NearbyUserAdded,
    NearbyUserMoved,
    NearbyUserRemoved,
    NearbyUsersUpdated,
    Error
}
