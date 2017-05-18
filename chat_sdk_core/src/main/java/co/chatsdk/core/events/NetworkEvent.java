package co.chatsdk.core.events;

import io.reactivex.functions.Predicate;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class NetworkEvent {

    final public EventType type;
    public Object payload;

    public NetworkEvent(EventType type) {
        this.type = type;
    }

    public NetworkEvent(EventType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public static NetworkEvent privateThreadAdded () {
        return new NetworkEvent(EventType.PrivateThreadAdded);
    }

    public static NetworkEvent privateThreadRemoved () {
        return new NetworkEvent(EventType.PrivateThreadRemoved);
    }

    public static NetworkEvent publicThreadAdded () {
        return new NetworkEvent(EventType.PublicThreadAdded);
    }

    public static NetworkEvent publicThreadRemoved () {
        return new NetworkEvent(EventType.PublicThreadRemoved);
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
