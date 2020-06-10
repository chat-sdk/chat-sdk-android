package sdk.chat.ui.update;

import java.util.Arrays;

import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;

public class ThreadUpdateAction extends UpdateAction {

    public enum Type {
        Update,
        UpdateMessage,
        Add,
        Remove,
        SoftReload,
        Reload
    }

    public Type type;
    public Thread thread;
    public Message message;
    public ThreadHolder holder;

    public ThreadUpdateAction(Type type) {
        this(type, null);
    }

    public ThreadUpdateAction(Type type, Thread thread) {
        this.thread = thread;
        this.type = type;
    }

    public ThreadUpdateAction(Type type, Thread thread, ThreadHolder holder) {
        this(type, thread);
        this.holder = holder;
    }

    public ThreadUpdateAction(Type type, Thread thread, Message message) {
        this(type, thread);
        this.message = message;
    }

    public boolean isDuplicate(ThreadUpdateAction action) {
        return type.equals(action.type) && thread.equals(action.thread);
    }

    public static ThreadUpdateAction add(Thread thread) {
        return new ThreadUpdateAction(Type.Add, thread);
    }

    public static ThreadUpdateAction remove(Thread thread) {
        return new ThreadUpdateAction(Type.Remove, thread);
    }

    public static ThreadUpdateAction update(Thread thread, ThreadHolder holder) {
        return new ThreadUpdateAction(Type.Update, thread, holder);
    }

    public static ThreadUpdateAction updateMessage(Message message) {
        return new ThreadUpdateAction(Type.UpdateMessage, message.getThread(), message);
    }

    public static ThreadUpdateAction softReload() {
        return new ThreadUpdateAction(Type.SoftReload);
    }

    public static ThreadUpdateAction reload() {
        return new ThreadUpdateAction(Type.Reload);
    }

    public Integer priority() {
        return Arrays.asList(Type.values()).indexOf(type);
    }

}
