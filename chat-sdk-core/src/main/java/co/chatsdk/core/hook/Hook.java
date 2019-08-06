package co.chatsdk.core.hook;

import java.util.HashMap;

import io.reactivex.Completable;

/**
 * Created by ben on 9/13/17.
 */

public class Hook {

    private Executor executor;

    public Hook (Executor executor) {
        this.executor = executor;
    }

    public Completable execute (HashMap<String, Object> data) {
        if(executor != null) {
            return executor.execute(data);
        }
        return Completable.complete();
    }

    public interface Executor {
        Completable execute (HashMap<String, Object> data);
    }

}
