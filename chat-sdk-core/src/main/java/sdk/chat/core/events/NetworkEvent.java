package sdk.chat.core.events;

import android.location.Location;

import androidx.annotation.Nullable;

import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.Progress;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class NetworkEvent {

    final public EventType type;
    protected Message message;
    protected ThreadX thread;
    protected User user;
    protected String text;
    protected Map<String, Object> data;
    protected boolean isOnline = false;
    protected MessageSendStatus messageSendstatus;
    protected Progress progress;

//    public static String MessageSendProgress = "MessageSendProgress";
//    public static String Progress = "Progress";
    public static String MessageId = "MessageId";
    public static String Error = "Error";
    public Location location;

    public NetworkEvent(EventType type) {
        this.type = type;
    }

    public NetworkEvent(EventType type, ThreadX thread) {
        this(type, thread, null, null);
    }

    public NetworkEvent(EventType type, ThreadX thread, Message message) {
        this(type, thread, message, null);
    }

    public NetworkEvent(EventType type, ThreadX thread, Message message, User user) {
        this.type = type;
        this.thread = thread;
        this.message = message;
        this.user = user;
    }

    public NetworkEvent(EventType type, ThreadX thread, Message message, User user, MessageSendStatus status) {
        this(type, thread, message, user);
        this.messageSendstatus = status;
    }

    public NetworkEvent(EventType type, ThreadX thread, Message message, User user, Progress progress) {
        this(type, thread, message, user);
        this.progress = progress;
    }

    public NetworkEvent(EventType type, ThreadX thread, Message message, User user, Map<String, Object> data) {
        this(type, thread, message, user);
        this.data = data;
    }

    public static NetworkEvent threadAdded(ThreadX thread) {
        return new NetworkEvent(EventType.ThreadAdded, thread);
    }

    public static NetworkEvent messageSendStatusChanged(Message message) {
        return new NetworkEvent(EventType.MessageSendStatusUpdated, message.getThread(), message, message.getSender(), message.getMessageStatus());
    }

    public static NetworkEvent messageProgressUpdated(Message message, Progress progress) {
        return new NetworkEvent(EventType.MessageProgressUpdated, message.getThread(), message, message.getSender(), progress);
    }

    public static NetworkEvent threadsUpdated() {
        return new NetworkEvent(EventType.ThreadsUpdated);
    }

    public static NetworkEvent threadRemoved(ThreadX thread) {
        return new NetworkEvent(EventType.ThreadRemoved, thread);
    }

//    public static NetworkEvent threadDetailsUpdated(Thread thread) {
//        return new NetworkEvent(EventType.ThreadDetailsUpdated, thread);
//    }

//    public static NetworkEvent threadMarkedRead(Thread thread) {
//        return new NetworkEvent(EventType.ThreadMarkedRead, thread);
//    }

    public static NetworkEvent threadMetaUpdated(ThreadX thread) {
        return new NetworkEvent(EventType.ThreadMetaUpdated, thread);
    }

    public static NetworkEvent messageAdded(Message message) {
        return new NetworkEvent(EventType.MessageAdded, message.getThread(), message);
    }

    @Deprecated
    public static NetworkEvent messageAdded(ThreadX thread, Message message) {
        return new NetworkEvent(EventType.MessageAdded, thread, message);
    }

    public static NetworkEvent messageUpdated(Message message) {
        return new NetworkEvent(EventType.MessageUpdated, message.getThread(), message);
    }

    public static NetworkEvent threadMessagesUpdated(ThreadX thread) {
        return new NetworkEvent(EventType.ThreadMessagesUpdated, thread);
    }

    @Deprecated
    public static NetworkEvent messageUpdated(ThreadX thread, Message message) {
        return new NetworkEvent(EventType.MessageUpdated, thread, message);
    }

    public static NetworkEvent messageRemoved(Message message) {
        return new NetworkEvent(EventType.MessageRemoved, message.getThread(), message);
    }

    public static NetworkEvent messageSendFailed(String messageId, Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put(MessageId, messageId);
        map.put(Error, e);
        return new NetworkEvent(EventType.MessageSendFailed, null, null, null, map);
    }

    @Deprecated
    public static NetworkEvent messageRemoved(ThreadX thread, Message message) {
        return new NetworkEvent(EventType.MessageRemoved, thread, message);
    }

    public static NetworkEvent threadUserUpdated(ThreadX thread, User user) {
        return new NetworkEvent(EventType.ThreadUserUpdated, thread, null, user);
    }

    public static NetworkEvent threadUserAdded(ThreadX thread, User user) {
        return new NetworkEvent(EventType.ThreadUserAdded, thread, null, user);
    }

    public static NetworkEvent threadUserRemoved(ThreadX thread, User user) {
        return new NetworkEvent(EventType.ThreadUserRemoved, thread, null, user);
    }

    public static NetworkEvent threadUsersRoleUpdated(ThreadX thread, User user) {
        return new NetworkEvent(EventType.ThreadUserRoleUpdated, thread, null, user);
    }

    public static NetworkEvent userMetaUpdated(User user) {
        return new NetworkEvent(EventType.UserMetaUpdated, null, null, user);
    }

    public static NetworkEvent userPresenceUpdated(User user) {
        return new NetworkEvent(EventType.UserPresenceUpdated, null, null, user);
    }

    public static NetworkEvent contactAdded(User user) {
        return new NetworkEvent(EventType.ContactAdded, null, null, user);
    }

    public static NetworkEvent contactDeleted(User user) {
        return new NetworkEvent(EventType.ContactDeleted, null, null, user);
    }

    public static NetworkEvent contactsUpdated() {
        return new NetworkEvent(EventType.ContactsUpdated);
    }

    public static NetworkEvent threadRead(ThreadX thread) {
        return new NetworkEvent(EventType.ThreadRead, thread);
    }

    public static NetworkEvent messageReadReceiptUpdated(Message message) {
        return new NetworkEvent(EventType.MessageReadReceiptUpdated, message.getThread(), message);
    }

    public static NetworkEvent messageDateUpdated(Message message) {
        return new NetworkEvent(EventType.MessageDateUpdated, message.getThread(), message);
    }

    public static NetworkEvent networkStateChanged(boolean isOnline) {
        NetworkEvent event = new NetworkEvent(EventType.NetworkStateChanged);
        event.isOnline = isOnline;
        return event;
    }

    @Deprecated
    public static NetworkEvent messageReadReceiptUpdated(ThreadX thread, Message message) {
        return new NetworkEvent(EventType.MessageReadReceiptUpdated, thread, message);
    }

    public static NetworkEvent typingStateChanged(String message, ThreadX thread) {
        NetworkEvent event = new NetworkEvent(EventType.TypingStateUpdated);
        event.text = message;
        event.thread = thread;
        return event;
    }

    public static NetworkEvent nearbyUserAdded(User user, Location location) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserAdded);
        event.user = user;
        event.location = location;
        return event;
    }

    public static NetworkEvent nearbyUserMoved(User user, Location location) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserMoved);
        event.user = user;
        event.location = location;
        return event;
    }

    public static NetworkEvent nearbyUserRemoved(User user) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserRemoved);
        event.user = user;
        return event;
    }

    public static NetworkEvent nearbyUsersUpdated() {
        return new NetworkEvent(EventType.NearbyUsersUpdated);
    }

    public static NetworkEvent logout() {
        return new NetworkEvent(EventType.Logout);
    }

//    public Predicate<NetworkEvent> filter() {
//        return new Predicate<NetworkEvent>() {
//            @Override
//            public boolean test(NetworkEvent networkEvent) throws Exception {
//                return networkEvent.type == type;
//            }
//        };
//    }

    public static Predicate<NetworkEvent> filterType(final EventType type) {
        return networkEvent -> networkEvent.type == type;
    }

    public static Predicate<NetworkEvent> filterRoleUpdated(final ThreadX thread) {
        return filterRoleUpdated(thread, null);
    }

    public static Predicate<NetworkEvent> filterPresence(final ThreadX thread) {
        return networkEvent -> {
            boolean match = networkEvent.typeIs(EventType.UserPresenceUpdated);
            return  match && thread.containsUser(networkEvent.getUser());
        };
    }

    public static Predicate<NetworkEvent> filterRoleUpdated(final ThreadX thread, @Nullable final User user) {
        return networkEvent -> {
            boolean match = networkEvent.typeIs(EventType.ThreadUserRoleUpdated);
            match = match && networkEvent.getThread().equalsEntity(thread);
            if (match) {
                if (user == null) {
                    return true;
                }
                return user.equalsEntity(networkEvent.getUser()) || ChatSDK.currentUser().equalsEntity(networkEvent.getUser());
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterType(final EventType... types) {
        return networkEvent -> {
            for(EventType type: types) {
                if(networkEvent.type == type)
                    return true;
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterMessageEntityID(final String entityID) {
        return networkEvent -> {
            if(networkEvent.getMessage() != null) {
//                Logger.debug("TT Filter: " + entityID + " , Message: " + networkEvent.getMessage().getEntityID());

                if (networkEvent.getMessage().equalsEntityID(entityID)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadEntityID(final String entityID) {
        return networkEvent -> {
            if(networkEvent.getThread() != null) {
                if (networkEvent.getThread().equalsEntityID(entityID)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterUserEntityID(final String entityID) {
        return networkEvent -> {
            if(networkEvent.getUser() != null) {
                if (networkEvent.getUser().equalsEntityID(entityID)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterCurrentUser() {
        return networkEvent -> {
            if(networkEvent.getUser() != null) {
                if(networkEvent.getUser().equalsEntityID(ChatSDK.currentUserID())) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadType(final int type) {
        return networkEvent -> {
            if(networkEvent.getThread() != null) {
                ThreadX thread = networkEvent.getThread();
                return thread.typeIs(type);
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadContainsUser(final ThreadX thread) {
        return networkEvent -> {
            if(networkEvent.getUser() != null && thread != null) {
                return thread.containsUser(networkEvent.getUser());
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadTypeOrNull(final int type) {
        return networkEvent -> {
            if(networkEvent.getThread() != null) {
                ThreadX thread = networkEvent.getThread();
                return thread.typeIs(type);
            }
            return true;
        };
    }

//    public static Predicate<NetworkEvent> threadMetaUpdated() {
//        return filterType(
////                EventType.ThreadDetailsUpdated,
////                EventType.ThreadUsersUpdated,
//                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
//        );
//    }

    protected static Predicate<NetworkEvent> filterThreadsUpdated() {
        return filterType(
//                EventType.ThreadDetailsUpdated,
                EventType.ThreadsUpdated,
                EventType.ThreadAdded,
                EventType.ThreadRemoved,
                EventType.ThreadUserAdded,
                EventType.ThreadUserRemoved,
                EventType.ThreadUserUpdated,
                EventType.MessageAdded,
                EventType.MessageRemoved,
                EventType.MessageUpdated,
                EventType.MessageReadReceiptUpdated,
                EventType.UserPresenceUpdated,
                EventType.TypingStateUpdated,
                EventType.Logout,
                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
        );
    }


    public static Predicate<NetworkEvent> filterPrivateThreadsUpdated() {
        return networkEvent -> filterThreadsUpdated().test(networkEvent) && filterThreadTypeOrNull(ThreadType.Private).test(networkEvent);
     }

    public static Predicate<NetworkEvent> filterPublicThreadsUpdated() {
        return networkEvent -> filterThreadsUpdated().test(networkEvent) && filterThreadTypeOrNull(ThreadType.Public).test(networkEvent);
    }

    public static Predicate<NetworkEvent> filterContactsChanged() {
        return filterType(
                EventType.ContactAdded,
                EventType.ContactDeleted,
                EventType.UserMetaUpdated,
                EventType.ContactsUpdated
        );
    }

//    public static Predicate<NetworkEvent> filterThreadUsersUpdated() {
//        return filterType(
//                EventType.ThreadUsersUpdated,
//                EventType.UserPresenceUpdated
//        );
//    }

//    public MessageSendProgress getMessageSendProgress() {
//        if (data != null && data.containsKey(MessageSendProgress)) {
//            return (MessageSendProgress) data.get(MessageSendProgress);
//        }
//        return null;
//    }

    public boolean typeIs(EventType... types) {
        for (EventType type: types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    public void debug() {
        Logger.warn(text);
    }

    public String debugText() {
        String text = "Event: " + type.toString();
        if (user != null) {
            text += ", " + user.getEntityID() + " - " + user.getName();
        }
        if (thread != null) {
            text += ", " + thread.getEntityID() + " - " + thread.getName();
        }
        if (message != null) {
            text += ", " + message.getEntityID() + " - " + message.getText();
        }
        return text;
    }

    public Message getMessage() {
        return message;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public ThreadX getThread() {
        if (thread != null) {
            return thread;
        }
        if (message != null) {
            return message.getThread();
        }
        return null;
    }

    public User getUser() {
        return user;
    }

    @Nullable
    public UserThreadLink getUserThreadLink() {
        if (getUser() != null && getThread() != null) {
            return getThread().getUserThreadLink(getUser().getId());
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public Map<String, Object> getData() {
        return data;
    }

    boolean isDuplicate(NetworkEvent event) {
        if (user != null) {
            if (user.equalsEntity(event.user)) {
                return true;
            }
        }
        if (thread != null) {
            if (thread.equalsEntity(event.thread)) {
                return true;
            }
        }
        if (message != null) {
            if (message.equalsEntity(event.message)) {
                return true;
            }
        }
        return false;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public MessageSendStatus getMessageSendStatus() {
        return messageSendstatus;
    }

    public Progress getProgress() {
        return progress;
    }
}
