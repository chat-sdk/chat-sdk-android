package sdk.guru.common;

import androidx.annotation.Nullable;

import io.reactivex.CompletableObserver;
import sdk.guru.common.RX;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class BiAction implements CompletableObserver {

    protected Consumer<Throwable> onComplete;

    public BiAction(@Nullable Consumer<Throwable> onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onComplete() {
        accept(null);
    }

    @Override
    public void onError(Throwable e) {
        accept(e);
    }

    public void accept(Throwable e) {
        if (onComplete != null) {
            try {
                onComplete.accept(e);
            } catch (Exception error) {}
        }
    }
}
