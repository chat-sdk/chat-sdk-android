package co.chatsdk.core.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ben on 9/29/17.
 */

public class ObservableConnector<T> {

    public void connect (Observable<T> observable, final ObservableEmitter<T> e) {
        observable.subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(T value) {
                e.onNext(value);
            }

            @Override
            public void onError(Throwable ex) {
                e.onError(ex);
            }

            @Override
            public void onComplete() {
                e.onComplete();
            }
        });
    }

}
