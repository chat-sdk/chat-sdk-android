package co.chatsdk.core;

import co.chatsdk.core.dao.core.BThread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.functions.Predicate;

/**
 * Created by benjaminsmiley-andrews on 16/05/2017.
 */

public class PredicateFactory {

    public static Predicate<NetworkEvent> type (final EventType type) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                return networkEvent.type == type;
            }
        };
    }

    public static Predicate<NetworkEvent> type (final EventType... types) {
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

    public static Predicate<NetworkEvent> threadEntityID (final String entityID) {
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

    public static Predicate<NetworkEvent> threadType (final int type) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                if(networkEvent.thread != null) {
                    BThread thread = (BThread) networkEvent.thread;
                    return thread.typeIs(type);
                }
                return false;
            }
        };
    }


}
