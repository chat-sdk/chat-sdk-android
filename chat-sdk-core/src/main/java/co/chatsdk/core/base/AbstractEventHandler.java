package co.chatsdk.core.base;

import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.EventHandler;
import co.chatsdk.core.utils.DisposableList;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public abstract class AbstractEventHandler implements EventHandler, Consumer<Throwable> {

    final protected PublishSubject<NetworkEvent> eventSource = PublishSubject.create();
    final protected PublishSubject<Throwable> errorSource = PublishSubject.create();

    protected DisposableList dm = new DisposableList();

    public PublishSubject<NetworkEvent> source () {
        return eventSource;
    }

    public Observable<NetworkEvent> sourceOnMain () {
        return source().hide().subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Throwable> errorSourceOnMain() {
        return errorSource.hide().subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void accept(Throwable t) throws Exception {
        errorSource.onNext(t);
    };


}
