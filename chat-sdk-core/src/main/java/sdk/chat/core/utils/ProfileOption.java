package sdk.chat.core.utils;

import android.app.Activity;

public class ProfileOption {

    public interface Executor {
        void execute(Activity activity, String userEntityID);
    }

    protected String name;
    protected Executor executor;
    protected Runnable onDismiss;

    public ProfileOption(String name, Executor executor) {
        this.name = name;
        this.executor = executor;
    }

    public void execute(Activity activity, String userEntityID) {
        if (executor != null) {
            executor.execute(activity, userEntityID);
        }
    }

    public void dismiss() {
        if (onDismiss != null) {
            onDismiss.run();
        }
    }

    public String getName() {
        return name;
    }

}
