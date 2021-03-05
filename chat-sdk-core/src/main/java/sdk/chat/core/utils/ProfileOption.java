package sdk.chat.core.utils;

import android.app.Activity;

import sdk.chat.core.dao.User;

public class ProfileOption {

    public interface Executor {
        void execute(Activity activity, String userEntityID);
    }

    public interface Decision {
        enum Type {
            isMe,
            notMe
        }
        boolean showFor(User user);
    }

    public class UserDecision implements Decision {

        Type type;

        public UserDecision() {
        }

        public UserDecision(Type type) {
            this.type = type;
        }

        @Override
        public boolean showFor(User user) {
            if (type == Type.isMe) {
                return user.isMe();
            }
            if (type == Type.notMe) {
                return !user.isMe();
            }
            return true;
        }
    }

    protected String name;
    protected Executor executor;
    protected Runnable onDismiss;
    protected Decision showForUser;

    public ProfileOption(String name, Executor executor) {
        this.name = name;
        this.executor = executor;
        this.showForUser = new UserDecision();
    }

    public ProfileOption(String name, Executor executor, Decision showForUser) {
        this.name = name;
        this.executor = executor;
        this.showForUser = showForUser;
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

    public boolean showForUser(User user) {
        return showForUser.showFor(user);
    }
}
