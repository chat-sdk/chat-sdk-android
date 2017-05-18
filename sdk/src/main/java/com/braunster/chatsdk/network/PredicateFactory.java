package com.braunster.chatsdk.network;

import com.braunster.chatsdk.dao.BThread;

import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.Observable;
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

    public static Predicate<NetworkEvent> threadEntityID (final String entityID) {
        return new Predicate<NetworkEvent>() {
            @Override
            public boolean test(NetworkEvent networkEvent) throws Exception {
                if(networkEvent.payload instanceof BThread) {
                    BThread thread = (BThread) networkEvent.payload;
                    if (thread.getEntityID().equals(thread.getEntityID())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }


}
