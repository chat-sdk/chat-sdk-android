package co.chatsdk.core.utils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ben on 4/26/18.
 */

public class CrashReportingObserver<T> extends CrashReporter implements Observer<T> {

    public CrashReportingObserver(DisposableList list) {
        super(list);
    }

    public CrashReportingObserver() {
    }

    @Override
    public void onSubscribe(Disposable d) {
        super.onSubscribe(d);
    }

    @Override
    public void onNext(Object o) {

    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
    }

    @Override
    public void onComplete() {

    }
}
