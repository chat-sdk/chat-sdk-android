package firestream.chat.firebase.rx;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.jakewharton.rxrelay2.ReplayRelay;

import io.reactivex.Observable;
import io.reactivex.Observer;
import sdk.guru.common.RX;


public class MultiRelay<T> extends Relay<T> {

    protected final PublishRelay<T> publishRelay = PublishRelay.create();
    protected final BehaviorRelay<T> behaviorRelay = BehaviorRelay.create();
    protected final ReplayRelay<T> replayRelay = ReplayRelay.create();

    public static <T> MultiRelay<T> create() {
        return new MultiRelay<T>();
    }

    @Override
    public void accept(T t) {

        // Allow them to only subscribe to new events
        publishRelay.accept(t);

        // Allow them to get a behavior subject too
        behaviorRelay.accept(t);

        // Allow them to replay all events
        replayRelay.accept(t);

    }

    @Override
    public boolean hasObservers() {
        return publishRelay.hasObservers() || behaviorRelay.hasObservers() || replayRelay.hasObservers();
    }

    /**
     * Get a events of only new events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * only 3 and 4 would be emitted to teh observer
     * @return a events of new events
     */
    public Observable<T> newEvents() {
        return publishRelay
                .hide().observeOn(RX.main());
    }

    /**
     * Get a events of all events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * 1, 2, 3, 4 would be emitted
     * @return a events of all events past and future
     */
    public Observable<T> pastAndNewEvents() {
        return replayRelay.hide().observeOn(RX.main());
    }

    /**
     * Get a events of the last and all future events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * 2, 3, 4 would be emitted
     * @return a events of the last emitted event and all future events
     */
    public Observable<T> currentAndNewEvents() {
        return behaviorRelay.hide().observeOn(RX.main());
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {

    }
}
