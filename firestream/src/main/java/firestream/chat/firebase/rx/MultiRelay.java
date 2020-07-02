package firestream.chat.firebase.rx;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import com.jakewharton.rxrelay2.ReplayRelay;

import io.reactivex.Observable;
import io.reactivex.Observer;
import sdk.guru.common.RX;

/**
 * The MultiQueueSubject type a versatile events object. It has a number of options:
 * The main function of this events type to cache events until there type a consumer available to
 * consume them. This means that if a new emitter type added, unconsumed events will be
 * emitted once and then new events will be emitted as they arise. For example:
 *
 * By calling {@link #newEvents()} the observer will only receive new events:
 *
 * Events: 1, 2, [A added], 3, 4
 * Output: 3, 4
 *
 * Only events that happened after the observer was added are emitted
 *
 * By calling {@link #pastAndNewEvents()} the observer receive all events past and new:
 *
 * Events: 1, 2, [A added], 3, 4
 * Output: 1, 2, 3, 4
 *
 * All events are emitted
 *
 * By calling {@link #currentAndNewEvents()} the observer receive the last event and all new events
 *
 * Events: 1, 2, [A added], 3, 4
 * Output: 2, 3, 4
 *
 * The previous event and then all new events will be emitted
 *
 * @param <T> the object type that will be emitted
 */
public class MultiRelay<T> extends Relay<T> {

    protected final PublishRelay<T> publishRelay = PublishRelay.create();
    protected final BehaviorRelay<T> behaviorRelay = BehaviorRelay.create();
    protected final ReplayRelay<T> replaySubject = ReplayRelay.create();

    public static <T> MultiRelay<T> create() {
        return new MultiRelay<T>();
    }

    @Override
    public void accept(T t) {

        // Allow them to only subscribe to new events
        publishRelay.accept(t);

        // Allow them to get a behavour subject too
        behaviorRelay.accept(t);

        // Allow them to replay all events
        replaySubject.accept(t);

    }

    @Override
    public boolean hasObservers() {
        return publishRelay.hasObservers() || behaviorRelay.hasObservers() || replaySubject.hasObservers();
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
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
        return replaySubject.hide().observeOn(RX.main());
    }

    /**
     * Get a events of the last and all future events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * 2, 3, 4 would be emitted
     * @return a events of the last emitted event and all future events
     */
    public Observable<T> currentAndNewEvents() {
        return behaviorRelay.hide().observeOn(RX.main());
    }

}
