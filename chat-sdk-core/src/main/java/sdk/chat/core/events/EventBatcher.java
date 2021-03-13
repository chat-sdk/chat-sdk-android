package sdk.chat.core.events;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import sdk.guru.common.DisposableMap;

public class EventBatcher {

    public interface Listener {
        void onNext(NetworkEvent event);
        void batchFinished();
    }

    public double interval;
    public Listener listener;
    public DisposableMap dm = new DisposableMap();

    protected int batchSize = 0;
    protected int threshold = 5;

    public EventBatcher(double thresholdMillis, Listener listener) {
        this.interval = thresholdMillis;
        this.listener = listener;
    }

    public void add(final NetworkEvent event) {
        add(event, false);
    }

    public void add(final NetworkEvent event, boolean passThrough) {
        if (batchSize < threshold || passThrough) {
            listener.onNext(event);
        }
        batchSize++;

        dm.dispose();

        // Keep a pulse so the batching will decay
        dm.add(Observable.interval((long) interval, TimeUnit.MILLISECONDS).subscribe(aLong -> {
            if (batchSize >= threshold) {
                listener.batchFinished();
            }
            batchSize = 0;
        }));
    }

    public void dispose() {
        dm.dispose();
    }
}
