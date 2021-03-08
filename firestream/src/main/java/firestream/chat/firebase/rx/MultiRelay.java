package firestream.chat.firebase.rx;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;
import com.jakewharton.rxrelay2.ReplayRelay;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import sdk.guru.common.RX;


public class MultiRelay<T> extends Relay<T> {

    /** An empty subscribers array to avoid allocating it all the time. */
    @SuppressWarnings("rawtypes")
    static final MultiRelayDisposable[] EMPTY = new MultiRelayDisposable[0];

    /** The array of currently subscribed subscribers. */
    final AtomicReference<MultiRelayDisposable<T>[]> subscribers;

    protected final BehaviorRelay<T> behaviorRelay = BehaviorRelay.create();
    protected final ReplayRelay<T> replayRelay = ReplayRelay.create();

    public static <T> MultiRelay<T> create() {
        return new MultiRelay<T>();
    }

    /**
     * Constructs a PublishRelay.
     */
//    @SuppressWarnings("unchecked")
    MultiRelay() {
        subscribers = new AtomicReference<MultiRelayDisposable<T>[]>(EMPTY);
    }

    @Override
    public void accept(T t) {

//        // Allow them to only subscribe to new events
//        publishRelay.accept(t);

        // Allow them to get a behavior subject too
        behaviorRelay.accept(t);

        // Allow them to replay all events
        replayRelay.accept(t);

        if (t == null) throw new NullPointerException("value == null");
        for (MultiRelayDisposable<T> s : subscribers.get()) {
            s.onNext(t);
        }
    }

    @Override
    public boolean hasObservers() {
        return subscribers.get().length != 0 || behaviorRelay.hasObservers() || replayRelay.hasObservers();
    }

    /**
     * Get a events of only new events. For example in the sequence: 1, 2, [subscribed], 3, 4,
     * only 3 and 4 would be emitted to teh observer
     * @return a events of new events
     */
    public Observable<T> newEvents() {
        return hide().observeOn(RX.main());
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
    protected void subscribeActual(Observer<? super T> t) {
        MultiRelayDisposable<T> ps = new MultiRelayDisposable<T>(t, this);
        t.onSubscribe(ps);
        add(ps);
        // if cancellation happened while a successful add, the remove() didn't work
        // so we need to do it again
        if (ps.isDisposed()) {
            remove(ps);
        }
    }

    /**
     * Adds the given subscriber to the subscribers array atomically.
     * @param ps the subscriber to add
     */
    void add(MultiRelayDisposable<T> ps) {
        for (;;) {
            MultiRelayDisposable<T>[] a = subscribers.get();
            int n = a.length;
            @SuppressWarnings("unchecked")
            MultiRelayDisposable<T>[] b = new MultiRelayDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;

            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    /**
     * Atomically removes the given subscriber if it is subscribed to the subject.
     * @param ps the subject to remove
     */
    @SuppressWarnings("unchecked")
    void remove(MultiRelayDisposable<T> ps) {
        for (;;) {
            MultiRelayDisposable<T>[] a = subscribers.get();
            if (a == EMPTY) {
                return;
            }

            int n = a.length;
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == ps) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            MultiRelayDisposable<T>[] b;

            if (n == 1) {
                b = EMPTY;
            } else {
                b = new MultiRelayDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (subscribers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    static final class MultiRelayDisposable<T> extends AtomicBoolean implements Disposable {

        private static final long serialVersionUID = 3562861878281475070L;
        /** The actual subscriber. */
        final Observer<? super T> downstream;
        /** The subject state. */
        final MultiRelay<T> parent;

        /**
         * Constructs a PublishSubscriber, wraps the actual subscriber and the state.
         * @param actual the actual subscriber
         * @param parent the parent PublishProcessor
         */
        MultiRelayDisposable(Observer<? super T> actual, MultiRelay<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            if (!get()) {
                downstream.onNext(t);
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }

}
