package co.chatsdk.core.hook;

import java.util.HashMap;

/**
 * Created by ben on 9/13/17.
 */

public class Hook {

    private Executor executor;

    public Hook (Executor executor) {
        this.executor = executor;
    }

    public void execute (HashMap<String, Object> data) {
        if(executor != null) {
            executor.execute(data);
        }
    }

    public interface Executor {
        void execute (HashMap<String, Object> data);
    }

}
