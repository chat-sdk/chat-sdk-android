package sdk.chat.core.events;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public enum EventType {
    ThreadAdded,
    ThreadRemoved,
    ThreadMetaUpdated,
    ThreadMessagesUpdated,
    MessageAdded,
    MessageUpdated,
    MessageRemoved,
    MessageProgressUpdated,
    MessageSendStatusUpdated,
    MessageSendFailed,
    MessageReadReceiptUpdated,
    MessageDateUpdated,

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
    NearbyUserAdded,
    NearbyUserMoved,
    NearbyUserRemoved,
    NearbyUsersUpdated,
    NetworkStateChanged,
    Error
}
