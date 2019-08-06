package co.chatsdk.core.message_action;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.chatsdk.core.dao.Message;
import io.reactivex.Completable;

public abstract class MessageAction {

    public enum Type {
        None,
        Delete,
        Forward,
        Copy
    }

    public WeakReference<Message> message;

    public Type type;
    public int titleResourceId;
    public int iconResourceId;
    public int colorId;
    public int successMessageId;

    public abstract Completable execute (Activity activity);

    public MessageAction (Message message) {
        this.message = new WeakReference<>(message);
    }

    public static List<MessageAction> asList (MessageAction... actions) {
        return Arrays.asList(actions);
    }
}
