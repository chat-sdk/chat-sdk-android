package sdk.chat.core.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class EventBatcher {
//
//    public Map<EventType, List<NetworkEvent>> eventMap = new HashMap<>();
//    protected DisposableMap dm = new DisposableMap();
//
//    public EventBatcher() {
//        // Capture the events as they are emitted
//        dm.add(ChatSDK.events().source().subscribe(networkEvent -> {
//            addEvent(networkEvent);
//        }));
//        dm.add(Observable.interval(1, TimeUnit.SECONDS).observeOn(RX.computation()).subscribe(aLong -> {
//
//        }));
//    }
//
//    protected void addEvent(NetworkEvent event) {
//        List<NetworkEvent> list = eventMap.get(event.type);
//        if (list == null) {
//            list = new ArrayList<>();
//            eventMap.put(event.type, list);
//        }
//        list.add(event);
//    }
//
//    protected void processBatch() {
//        Map<EventType, NetworkEvent> batch = new HashMap<>(eventMap);
//        eventMap.clear();
//
//        // Remove duplicates
//        for (EventType type: eventMap.keySet()) {
//            List<NetworkEvent> list = eventMap.get(type);
//            if (list != null) {
//                int size = list.size();
//                NetworkEvent a;
//                NetworkEvent b;
//                for (int i = 0; i < size; i++) {
//                    a = list.get(i);
//                    for (int j = 0; j < size; j++) {
//                        b = list.get(j);
//
//                    }
//                }
//            }
//        }
//
//    }

}
