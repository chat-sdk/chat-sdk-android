package sdk.chat.core.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import sdk.guru.common.DisposableMap;

public class EventBatcher {

    public interface Listener {
        void onNext(NetworkEvent event);
        void batchFinished();
    }

    List<Date> points = new ArrayList<>();
    public double maxPoints = 2;
    public double threshold;
    public double average = 0;
    public double lastAverage = 0;
    public Listener listener;
    public DisposableMap dm = new DisposableMap();


    public EventBatcher(double thresholdMillis, Listener listener) {
        this.threshold = thresholdMillis;
        this.listener = listener;

    }

    public void add(final NetworkEvent event) {
        points.add(new Date());
        if (points.size() > maxPoints) {
            points.remove(0);
        }
        calculateAverage();

        dm.dispose();

        if ((lastAverage < threshold && average > threshold)) {
            listener.batchFinished();
        } else if (average > threshold) {
            listener.onNext(event);
        } else {
            dm.add(Observable.interval((long) threshold, TimeUnit.MILLISECONDS).subscribe(aLong -> {
                batchFinished();
            }));
        }
    }

    public void batchFinished() {
        dm.dispose();
        average = threshold;
        listener.batchFinished();
    }

    public void calculateAverage() {
        lastAverage = average;
        if (points.size() > 1) {
            average = (points.get(points.size() - 1).getTime() - points.get(0).getTime())/maxPoints;
        } else {
            average = threshold;
        }
    }



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
