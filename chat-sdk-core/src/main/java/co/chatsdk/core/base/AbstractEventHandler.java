package co.chatsdk.core.base;

import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.utils.DisposableList;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class AbstractEventHandler implements EventHandler {

    final protected PublishSubject<NetworkEvent> eventSource = PublishSubject.create();
    protected DisposableList disposableList = new DisposableList();

    public PublishSubject<NetworkEvent> source () {
        return eventSource;
    }

    public Observable<NetworkEvent> sourceOnMain () {
        return source().subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

}
