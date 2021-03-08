package firestream.chat.test;

import sdk.guru.common.DisposableMap;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public abstract class Test implements Consumer<Throwable> {

    protected String name;
    protected DisposableMap dm = new DisposableMap();
    protected Emitter<Result> emitter;
    protected boolean failed = false;

    public Test(String name) {
        this.name = name;
    }

    public abstract Observable<Result> run();

    public void failure(String message) {
        if (emitter != null) {
            emitter.onNext(Result.failure(this, message));
        }
        failed = true;
    }

    public void complete() {
        if (emitter != null) {
            if (!failed) {
                emitter.onNext(Result.success(this));
            }
            emitter.onComplete();
        }
        dispose();
    }

    public void dispose() {
        dm.dispose();
    }

    public void manage(Emitter<Result> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        failure(throwable.getLocalizedMessage());
        complete();
    }

}
