package sdk.chat.core.base;

import java.util.HashMap;

import io.reactivex.Scheduler;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.EventHandler;
import sdk.chat.core.hook.Executor;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.notifications.NotificationDisplayHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.guru.common.DisposableMap;
import io.reactivex.Observable;
import sdk.guru.common.RX;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import sdk.guru.common.RX;
import io.reactivex.subjects.PublishSubject;

public abstract class AbstractEventHandler implements EventHandler {

    final protected PublishSubject<NetworkEvent> eventSource = PublishSubject.create();
    final protected PublishSubject<Throwable> errorSource = PublishSubject.create();

    final protected DisposableMap dm = new DisposableMap();

    public AbstractEventHandler() {
        dm.add(source().filter(NetworkEvent.filterType(EventType.Logout)).subscribe(networkEvent -> {
            dm.dispose();
        }, this));
    }

    public PublishSubject<NetworkEvent> source() {
        return eventSource;
    }

    public Observable<NetworkEvent> sourceOnMain () {
        return source().hide().observeOn(RX.main());
    }

    public Observable<NetworkEvent> sourceOnBackground () {
        return source().hide().observeOn(RX.computation());
    }

    public Observable<NetworkEvent> sourceOn (Scheduler scheduler) {
        return source().hide().observeOn(scheduler);
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
        errorSource.onNext(e);
    }


}
