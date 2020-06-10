package sdk.chat.core.hook;

import java.util.HashMap;

import io.reactivex.Completable;

/**
 * Created by ben on 9/13/17.
 */

public class Hook implements AsyncExecutor {

    public Executor executor;
    public AsyncExecutor asyncExecutor;
    public boolean removeOnFire;

    protected Hook(Executor executor) {
        this(executor, false);
    }

    protected Hook(Executor executor, boolean removeOnFire) {
        this.executor = executor;
        this.removeOnFire = removeOnFire;
    }

    protected Hook(AsyncExecutor executor) {
        this(executor, false);
    }

    protected Hook(AsyncExecutor executor, boolean removeOnFire) {
        this.asyncExecutor = executor;
        this.removeOnFire = removeOnFire;
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
    public static Hook sync(Executor executor, boolean removeOnFire) {
        return new Hook(executor);
    }

    public static Hook async(AsyncExecutor executor) {
        return new Hook(executor);
    }
    public static Hook async(AsyncExecutor executor, boolean removeOnFire) {
        return new Hook(executor);
    }

}
