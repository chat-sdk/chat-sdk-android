package sdk.chat.core.base;

import com.jakewharton.rxrelay2.PublishRelay;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.EventHandler;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public abstract class AbstractEventHandler implements EventHandler {

    final protected PublishRelay<NetworkEvent> incomingRelay = PublishRelay.create();
    protected Disposable incomingRelayDisposable;


    final protected PublishRelay<NetworkEvent> priorityEventSource = PublishRelay.create();

    final protected PublishRelay<NetworkEvent> eventSource = PublishRelay.create();
    final protected PublishRelay<Throwable> errorSource = PublishRelay.create();

    final protected DisposableMap dm = new DisposableMap();

    public AbstractEventHandler() {
        dm.add(source().filter(NetworkEvent.filterType(EventType.Logout)).subscribe(networkEvent -> {
            dm.dispose();
        }, this));
        incomingRelayDisposable = incomingRelay.subscribe(networkEvent -> {
            // Pass the event first to the priority relay
            priorityEventSource.accept(networkEvent);

            // Pass to standard relay
            eventSource.accept(networkEvent);
        });
    }

    public PublishRelay<NetworkEvent> source() {
//        return eventSource;
        return incomingRelay;
    }

    @Override
    public Observable<NetworkEvent> sourceOnMain() {
        return source().hide().observeOn(RX.main());
    }

    @Override
    public Observable<NetworkEvent> sourceOnBackground () {
        return eventSource.hide().observeOn(RX.computation());
    }

    @Override
    public Observable<NetworkEvent> sourceOnSingle() {
        return eventSource.hide().observeOn(RX.single());
    }

    public Observable<NetworkEvent> prioritySourceOnMain() {
        return priorityEventSource.hide().observeOn(RX.main());
    }

    public Observable<NetworkEvent> prioritySourceOnBackground () {
        return priorityEventSource.hide().observeOn(RX.computation());
    }

    @Override
    public Observable<NetworkEvent> prioritySourceOnSingle() {
        return priorityEventSource.hide().observeOn(RX.single());
    }

    public Observable<NetworkEvent> sourceOn(Scheduler scheduler) {
        return eventSource.hide().observeOn(scheduler);
    }

    public Observable<Throwable> errorSourceOnMain() {
        return errorSource.hide().observeOn(RX.main());
    }

    public void accept(Throwable t) throws Exception {
        onError(t);
    };

    public void disposeOnLogout(Disposable d) {
        dm.add(d);
    }

    public void onSubscribe(@NonNull Disposable d) {
        dm.add(d);
    }

    public void onComplete() {}

    public void onError(@NonNull Throwable e) {
        errorSource.accept(e);
    }

    public void stop() {
        dm.dispose();
    }

}
