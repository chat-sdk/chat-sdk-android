package co.chatsdk.core.hook;

import java.util.HashMap;
import java.util.concurrent.Callable;

import co.chatsdk.core.events.NetworkEvent;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;

/**
 * Created by ben on 9/13/17.
 */

public class Hook implements AsyncExecutor {

    public Executor executor;
    public AsyncExecutor asyncExecutor;

    protected Hook(Executor executor) {
        this.executor = executor;
    }

    protected Hook(AsyncExecutor executor) {
        this.asyncExecutor = executor;
    }

    public Completable executeAsync (HashMap<String, Object> data) {
        return Completable.defer(() -> {
            if (asyncExecutor != null) {
                return asyncExecutor.executeAsync(data);
            }
            if (executor != null) {
                executor.execute(data);
            }
            return Completable.complete();
        });
    }

    public static Hook sync(Executor executor) {
        return new Hook(executor);
    }

    public static Hook async(AsyncExecutor executor) {
        return new Hook(executor);
    }

}
