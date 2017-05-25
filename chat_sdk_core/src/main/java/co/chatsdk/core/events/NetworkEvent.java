package co.chatsdk.core.events;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import io.reactivex.functions.Predicate;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class NetworkEvent {

    final public EventType type;
    public BMessage message;
    public BThread thread;
    public BUser user;

    public NetworkEvent(EventType type) {
        this.type = type;
    }

    public NetworkEvent(EventType type, BThread thread) {
        this(type, thread, null, null);
    }

    public NetworkEvent(EventType type, BThread thread, BMessage message) {
        this(type, thread, message, null);
    }

    public NetworkEvent(EventType type, BThread thread, BMessage message, BUser user) {
        this.type = type;
        this.thread = thread;
        this.message = message;
        this.user = user;
    }

    public static NetworkEvent privateThreadAdded (BThread thread) {
        return new NetworkEvent(EventType.PrivateThreadAdded, thread);
    }

    public static NetworkEvent privateThreadRemoved (BThread thread) {
        return new NetworkEvent(EventType.PrivateThreadRemoved, thread);
    }

    public static NetworkEvent publicThreadAdded (BThread thread) {
        return new NetworkEvent(EventType.PublicThreadAdded, thread);
    }

    public static NetworkEvent publicThreadRemoved (BThread thread) {
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

    public static NetworkEvent threadDetailsUpdated (BThread thread) {
        return new NetworkEvent(EventType.ThreadDetailsUpdated, thread);
    }

    public static NetworkEvent messageAdded (BThread thread, BMessage message) {
        return new NetworkEvent(EventType.MessageAdded, thread, message);
    }

    public static NetworkEvent threadUsersChanged (BThread thread, BUser user) {
        return new NetworkEvent(EventType.ThreadUsersChanged, thread, null, user);
    }

    public static NetworkEvent userMetaUpdated (BUser user) {
        return new NetworkEvent(EventType.UserMetaUpdated, null, null, user);
    }

    public Predicate<NetworkEvent> filter () {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                return networkEvent.type == type;
            }
        };
    }


//    public static Event Event () {
//        return new Event(EventType.);
//    }


}
