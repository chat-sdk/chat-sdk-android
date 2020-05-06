package sdk.chat.core.events;

import android.location.Location;

import org.pmw.tinylog.Logger;

import java.util.HashMap;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendProgress;
import io.reactivex.functions.Predicate;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class NetworkEvent {

    final public EventType type;
    protected Message message;
    protected Thread thread;
    protected User user;
    protected String text;
    protected HashMap<String, Object> data;

    public static String MessageSendProgress = "MessageSendProgress";
    public Location location;

    public NetworkEvent(EventType type) {
        this.type = type;
    }

    public NetworkEvent(EventType type, Thread thread) {
        this(type, thread, null, null);
    }

    public NetworkEvent(EventType type, Thread thread, Message message) {
        this(type, thread, message, null);
    }

    public NetworkEvent(EventType type, Thread thread, Message message, User user) {
        this(type, thread, message, user, null);
    }

    public NetworkEvent(EventType type, Thread thread, Message message, User user, HashMap<String, Object> data) {
        this.type = type;
        this.thread = thread;
        this.message = message;
        this.user = user;
        this.data = data;
    }

    public static NetworkEvent threadAdded(Thread thread) {
        return new NetworkEvent(EventType.ThreadAdded, thread);
    }

    public static NetworkEvent messageSendStatusChanged(MessageSendProgress progress) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(MessageSendProgress, progress);
        return new NetworkEvent(EventType.MessageSendStatusUpdated, progress.message.getThread(), progress.message, progress.message.getSender(), data);
    }

    public static NetworkEvent threadRemoved(Thread thread) {
        return new NetworkEvent(EventType.ThreadRemoved, thread);
    }

    public static NetworkEvent threadDetailsUpdated(Thread thread) {
        return new NetworkEvent(EventType.ThreadDetailsUpdated, thread);
    }

//    public static NetworkEvent threadMarkedRead(Thread thread) {
//        return new NetworkEvent(EventType.ThreadMarkedRead, thread);
//    }

    public static NetworkEvent threadMetaUpdated(Thread thread) {
        return new NetworkEvent(EventType.ThreadMetaUpdated, thread);
    }

    public static NetworkEvent messageAdded(Message message) {
        return new NetworkEvent(EventType.MessageAdded, message.getThread(), message);
    }

    @Deprecated
    public static NetworkEvent messageAdded(Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageAdded, thread, message);
    }

    public static NetworkEvent messageUpdated(Message message) {
        return new NetworkEvent(EventType.MessageUpdated, message.getThread(), message);
    }

    @Deprecated
    public static NetworkEvent messageUpdated(Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageUpdated, thread, message);
    }

    public static NetworkEvent messageRemoved(Message message) {
        return new NetworkEvent(EventType.MessageRemoved, message.getThread(), message);
    }

    @Deprecated
    public static NetworkEvent messageRemoved(Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageRemoved, thread, message);
    }

    public static NetworkEvent threadUsersChanged(Thread thread, User user) {
        return new NetworkEvent(EventType.ThreadUsersUpdated, thread, null, user);
    }

    public static NetworkEvent threadUsersRoleChanged(Thread thread, User user) {
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

    public static NetworkEvent threadRead(Thread thread) {
        return new NetworkEvent(EventType.ThreadRead, thread);
    }

    public static NetworkEvent messageReadReceiptUpdated(Message message) {
        return new NetworkEvent(EventType.MessageReadReceiptUpdated, message.getThread(), message);
    }

    @Deprecated
    public static NetworkEvent messageReadReceiptUpdated(Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageReadReceiptUpdated, thread, message);
    }

    public static NetworkEvent typingStateChanged(String message, Thread thread) {
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

    public static Predicate<NetworkEvent> filterType(final EventType... types) {
        return networkEvent -> {
            for(EventType type: types) {
                if(networkEvent.type == type)
                    return true;
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
                Thread thread = networkEvent.getThread();
                return thread.typeIs(type);
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadTypeOrNull(final int type) {
        return networkEvent -> {
            if(networkEvent.getThread() != null) {
                Thread thread = networkEvent.getThread();
                return thread.typeIs(type);
            }
            return true;
        };
    }
    public static Predicate<NetworkEvent> threadDetailsUpdated() {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.ThreadUsersUpdated,
                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
        );
    }

    public static Predicate<NetworkEvent> threadsUpdated() {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.ThreadAdded,
                EventType.ThreadRemoved,
                EventType.ThreadUsersUpdated,
                EventType.MessageAdded,
                EventType.MessageRemoved,
                EventType.MessageReadReceiptUpdated,
                EventType.UserPresenceUpdated,
                EventType.TypingStateUpdated,
                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
        );
    }


    public static Predicate<NetworkEvent> filterPrivateThreadsUpdated() {
        return networkEvent -> threadsUpdated().test(networkEvent) && filterThreadTypeOrNull(ThreadType.Private).test(networkEvent);
     }

    public static Predicate<NetworkEvent> filterPublicThreadsUpdated() {
        return networkEvent -> threadsUpdated().test(networkEvent) && filterThreadTypeOrNull(ThreadType.Public).test(networkEvent);
    }

    public static Predicate<NetworkEvent> filterContactsChanged() {
        return filterType(
                EventType.ContactAdded,
                EventType.ContactDeleted,
                EventType.UserMetaUpdated,
                EventType.ContactsUpdated
        );
    }

    public static Predicate<NetworkEvent> threadUsersUpdated() {
        return filterType(
                EventType.ThreadUsersUpdated,
                EventType.UserPresenceUpdated
        );
    }

    public MessageSendProgress getMessageSendProgress() {
        if (data != null && data.containsKey(MessageSendProgress)) {
            return (MessageSendProgress) data.get(MessageSendProgress);
        }
        return null;
    }

    public boolean typeIs(EventType... types) {
        for (EventType type: types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    public void debug() {
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
        Logger.debug(text);
    }

    public Message getMessage() {
        return message;
    }

    public Thread getThread() {
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

    public String getText() {
        return text;
    }

    public HashMap<String, Object> getData() {
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
}
