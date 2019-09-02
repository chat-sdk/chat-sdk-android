package co.chatsdk.core.events;

import android.location.Location;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.functions.Predicate;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class NetworkEvent {

    final public EventType type;
    public Message message;
    public Thread thread;
    public User user;
    public String text;
    public HashMap<String, Object> data;

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

    public static NetworkEvent threadAdded (Thread thread) {
        return new NetworkEvent(EventType.ThreadAdded, thread);
    }

    public static NetworkEvent messageSendStatusChanged (MessageSendProgress progress) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(MessageSendProgress, progress);

        System.out.println("Create: " + progress.message.getMessageStatus());
        return new NetworkEvent(EventType.MessageSendStatusChanged, progress.message.getThread(), progress.message, progress.message.getSender(), data);
    }

    public static NetworkEvent threadRemoved (Thread thread) {
        return new NetworkEvent(EventType.ThreadRemoved, thread);
    }

    public static NetworkEvent followerAdded () {
        return new NetworkEvent(EventType.FollowerAdded);
    }

    public static NetworkEvent followerRemoved () {
        return new NetworkEvent(EventType.FollowerRemoved);
    }

    public static NetworkEvent followingAdded () {
        return new NetworkEvent(EventType.FollowingAdded);
    }

    public static NetworkEvent followingRemoved () {
        return new NetworkEvent(EventType.FollowingRemoved);
    }

    public static NetworkEvent threadDetailsUpdated (Thread thread) {
        return new NetworkEvent(EventType.ThreadDetailsUpdated, thread);
    }

    public static NetworkEvent threadLastMessageUpdated (Thread thread) {
        return new NetworkEvent(EventType.ThreadLastMessageUpdated, thread);
    }

    public static NetworkEvent threadMetaUpdated (Thread thread) {
        return new NetworkEvent(EventType.ThreadMetaUpdated, thread);
    }

    public static NetworkEvent messageAdded (Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageAdded, thread, message);
    }

    public static NetworkEvent messageRemoved (Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageRemoved, thread, message);
    }

    public static NetworkEvent threadUsersChanged (Thread thread, User user) {
        return new NetworkEvent(EventType.ThreadUsersChanged, thread, null, user);
    }


    public static NetworkEvent userMetaUpdated (User user) {
        return new NetworkEvent(EventType.UserMetaUpdated, null, null, user);
    }

    public static NetworkEvent userPresenceUpdated (User user) {
        return new NetworkEvent(EventType.UserPresenceUpdated, null, null, user);
    }

    public static NetworkEvent contactAdded (User user) {
        return new NetworkEvent(EventType.ContactAdded, null, null, user);
    }

    public static NetworkEvent contactDeleted (User user) {
        return new NetworkEvent(EventType.ContactDeleted, null, null, user);
    }

    public static NetworkEvent contactsUpdated () {
        return new NetworkEvent(EventType.ContactsUpdated);
    }

    public static NetworkEvent threadRead (Thread thread) {
        return new NetworkEvent(EventType.ThreadRead, thread);
    }

    public static NetworkEvent threadReadReceiptUpdated (Thread thread, Message message) {
        return new NetworkEvent(EventType.ThreadReadReceiptUpdated, thread, message);
    }

    public static NetworkEvent typingStateChanged (String message, Thread thread) {
        NetworkEvent event = new NetworkEvent(EventType.TypingStateChanged);
        event.text = message;
        event.thread = thread;
        return event;
    }

    public static NetworkEvent nearbyUserAdded (User user, Location location) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserAdded);
        event.user = user;
        event.location = location;
        return event;
    }

    public static NetworkEvent nearbyUserMoved (User user, Location location) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserMoved);
        event.user = user;
        event.location = location;
        return event;
    }

    public static NetworkEvent nearbyUserRemoved (User user) {
        NetworkEvent event = new NetworkEvent(EventType.NearbyUserRemoved);
        event.user = user;
        return event;
    }

    public static NetworkEvent nearbyUsersUpdated () {
        return new NetworkEvent(EventType.NearbyUsersUpdated);
    }

    public static NetworkEvent logout () {
        return new NetworkEvent(EventType.Logout);
    }

//    public Predicate<NetworkEvent> filter () {
//        return new Predicate<NetworkEvent>() {
//            @Override
//            public boolean test(NetworkEvent networkEvent) throws Exception {
//                return networkEvent.type == type;
//            }
//        };
//    }

    public static Predicate<NetworkEvent> filterType (final EventType type) {
        return networkEvent -> networkEvent.type == type;
    }

    public static Predicate<NetworkEvent> filterType (final EventType... types) {
        return networkEvent -> {
            for(EventType type: types) {
                if(networkEvent.type == type)
                    return true;
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadEntityID (final String entityID) {
        return networkEvent -> {
            if(networkEvent.thread != null) {
                if (networkEvent.thread.equalsEntityID(entityID)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> filterThreadType (final int type) {
        return networkEvent -> {
            if(networkEvent.thread != null) {
                Thread thread = networkEvent.thread;
                return thread.typeIs(type);
            }
            return false;
        };
    }

    public static Predicate<NetworkEvent> threadDetailsUpdated () {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.ThreadUsersChanged,
                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
        );
    }

    public static Predicate<NetworkEvent> threadsUpdated () {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.ThreadAdded,
                EventType.ThreadRemoved,
                EventType.ThreadLastMessageUpdated,
                EventType.ThreadUsersChanged,
                EventType.MessageAdded,
                EventType.MessageRemoved,
                EventType.UserMetaUpdated // Be careful to check that the user is a member of the thread...
        );
    }


    public static Predicate<NetworkEvent> filterPrivateThreadsUpdated () {
        return networkEvent -> threadsUpdated().test(networkEvent) && filterThreadType(ThreadType.Private).test(networkEvent);
     }

    public static Predicate<NetworkEvent> filterPublicThreadsUpdated () {
        return networkEvent -> threadsUpdated().test(networkEvent) && filterThreadType(ThreadType.Public).test(networkEvent);
    }

    public static Predicate<NetworkEvent> filterContactsChanged () {
        return filterType(
                EventType.ContactAdded,
                EventType.ContactDeleted,
                EventType.UserMetaUpdated,
                EventType.ContactsUpdated
        );
    }

    public static Predicate<NetworkEvent> threadUsersUpdated () {
        return filterType(
                EventType.ThreadUsersChanged,
                EventType.UserPresenceUpdated
        );
    }

    public MessageSendProgress getMessageSendProgress() {
        if (data != null && data.containsKey(MessageSendProgress)) {
            return (MessageSendProgress) data.get(MessageSendProgress);
        }
        return null;
    }

}
