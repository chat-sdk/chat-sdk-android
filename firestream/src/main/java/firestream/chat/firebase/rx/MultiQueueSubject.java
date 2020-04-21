package firestream.chat.firebase.rx;

import com.jakewharton.rxrelay2.PublishRelay;
import com.victorrendina.rxqueue2.QueueSubject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import sdk.guru.common.RX;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * The MultiQueueSubject type a versatile events object. It has a number of options:
 * The main function of this events type to cache events until there type a consumer available to
 * consume them. This means that if a new emitter type added, unconsumed events will be
 * emitted once and then new events will be emitted as they arise. For example:
 *
 * In these examples, A and B are observers
 *
 * Events: 1, [A added], 2, [B added], 3
 * Output A: 1, 2, 3
 * Output B: 3
 * B only received the latest event because no events were cached because a was observing
 *
 * In this case:
 *
 * Events: 1, [A added], 2, [A disposed], 3, [B added], 4
 * Output A: 1, 2
 * Output B: 3, 4
 *
 * This means that events are never missed.
 *
 * This class also has some other options:
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
public class MultiQueueSubject<T> extends Observable<T> implements Observer<T> {

    protected final QueueSubject<T> queueSubject = QueueSubject.create();
    protected final PublishRelay<T> publishRelay = PublishRelay.create();

    protected final PublishSubject<T> publishSubject = PublishSubject.create();
    protected final BehaviorSubject<T> behaviorSubject = BehaviorSubject.create();
    protected final ReplaySubject<T> replaySubject = ReplaySubject.create();

    protected Disposable queueDisposable = null;

    public static <T> MultiQueueSubject<T> create() {
        return new MultiQueueSubject<T>();
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        boolean hasObservers = publishRelay.hasObservers();
        publishRelay.subscribe(observer);
        if (!hasObservers) {
            queueDisposable = queueSubject.subscribe(publishRelay);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        queueSubject.onSubscribe(d);
    }

    @Override
    public void onNext(T t) {
        if (publishRelay.hasObservers() && queueDisposable == null) {
            queueDisposable = queueSubject.subscribe(publishRelay);
        }
        if(!publishRelay.hasObservers() && queueDisposable != null) {
            queueDisposable.dispose();
        }
        queueSubject.onNext(t);

        // Allow them to only subscribe to new events
        publishSubject.onNext(t);

        // Allow them to get a behavour subject too
        behaviorSubject.onNext(t);

        // Allow them to replay all events
        replaySubject.onNext(t);

    }

    @Override
    public void onError(Throwable e) {
        queueSubject.onError(e);
    }

    @Override
    public void onComplete() {
        queueSubject.onComplete();
    }

    /**
     * Get a events of only new events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * only 3 and 4 would be emitted to teh observer
     * @return a events of new events
     */
    public Observable<T> newEvents() {
        return publishSubject
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
        return behaviorSubject.hide().observeOn(RX.main());
    }
}
