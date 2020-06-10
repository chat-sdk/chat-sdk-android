package sdk.chat.core.events;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public enum EventType {

    ThreadAdded,
    ThreadRemoved,
    ThreadDetailsUpdated,
    ThreadMetaUpdated,
    MessageAdded,
    MessageUpdated,
    MessageRemoved,
    MessageSendStatusUpdated,
    ThreadUsersUpdated,
    ThreadUserRoleUpdated,
//    ThreadMarkedRead,
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
