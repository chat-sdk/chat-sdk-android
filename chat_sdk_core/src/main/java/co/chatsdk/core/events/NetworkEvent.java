package co.chatsdk.core.events;

import android.net.Network;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
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
        this.type = type;
        this.thread = thread;
        this.message = message;
        this.user = user;
    }

    public static NetworkEvent privateThreadAdded (Thread thread) {
        return new NetworkEvent(EventType.PrivateThreadAdded, thread);
    }

    public static NetworkEvent privateThreadRemoved (Thread thread) {
        return new NetworkEvent(EventType.PrivateThreadRemoved, thread);
    }

    public static NetworkEvent publicThreadAdded (Thread thread) {
        return new NetworkEvent(EventType.PublicThreadAdded, thread);
    }

    public static NetworkEvent publicThreadRemoved (Thread thread) {
        return new NetworkEvent(EventType.PublicThreadRemoved, thread);
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

    public static NetworkEvent messageAdded (Thread thread, Message message) {
        return new NetworkEvent(EventType.MessageAdded, thread, message);
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

    public static NetworkEvent contactChanged (User user) {
        return new NetworkEvent(EventType.ContactChanged, null, null, user);
    }

    public static NetworkEvent contactsUpdated () {
        return new NetworkEvent(EventType.ContactsUpdated);
    }

    public static NetworkEvent typingStateChanged (String message, Thread thread) {
        NetworkEvent event = new NetworkEvent(EventType.TypingStateChanged);
        event.text = message;
        event.thread = thread;
        return event;
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
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                return networkEvent.type == type;
            }
        };
    }

    public static Predicate<NetworkEvent> filterType (final EventType... types) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                for(EventType type: types) {
                    if(networkEvent.type == type)
                        return true;
                }
                return false;
            }
        };
    }

    public static Predicate<NetworkEvent> filterThreadEntityID (final String entityID) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                if(networkEvent.thread != null) {
                    if (networkEvent.thread.getEntityID().equals(entityID)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Predicate<NetworkEvent> filterThreadType (final int type) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                if(networkEvent.thread != null) {
                    Thread thread = (Thread) networkEvent.thread;
                    return thread.typeIs(type);
                }
                return false;
            }
        };
    }

    public static Predicate<NetworkEvent> filterPrivateThreadsUpdated () {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.PrivateThreadAdded,
                EventType.PrivateThreadRemoved,
                EventType.MessageAdded,
                EventType.ThreadUsersChanged
        );
    }

    public static Predicate<NetworkEvent> filterPublicThreadsUpdated () {
        return filterType(
                EventType.ThreadDetailsUpdated,
                EventType.PublicThreadAdded,
                EventType.PublicThreadRemoved,
                EventType.MessageAdded
        );
    }

    public static Predicate<NetworkEvent> filterContactsChanged () {
        return filterType(
                EventType.ContactChanged,
                EventType.ContactAdded,
                EventType.ContactDeleted,
                EventType.ContactsUpdated,
                EventType.UserPresenceUpdated
        );
    }
}
