package sdk.chat.firebase.adapter.wrappers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.FirebasePaths;
import io.reactivex.Completable;
import sdk.guru.common.RX;

public class ThreadDeleter {

    Thread thread;

    public ThreadDeleter (Thread thread) {
        this.thread = thread;
    }

    public Completable execute() {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Public)) {
                return Completable.complete();
            }
            else {
                if (thread.typeIs(ThreadType.Private1to1)) {
                    return deleteMessages().andThen(deleteOneToOneThread());
                } else {
                    return deleteMessages().andThen(ChatSDK.thread().removeUsersFromThread(thread, ChatSDK.currentUser()));
                }
            }
        }).subscribeOn(RX.io());
    }

    public Completable deleteMessages() {
        return Completable.create(emitter -> {
            List<Message> messages = new ArrayList<>(thread.getMessages());

            for (Message m : messages) {
                thread.removeMessage(m, false);
                m.delete();
            }
            thread.update();
            emitter.onComplete();
        }).subscribeOn(RX.io());
    }

    protected Completable deleteOneToOneThread() {
        return Completable.create(emitter -> {

            final User currentUser = ChatSDK.currentUser();

            DatabaseReference currentThreadUser = FirebasePaths.threadUsersRef(thread.getEntityID())
                    .child(currentUser.getEntityID());

            thread.setDeleted(true);
            thread.update();

            HashMap<String, Object> value = new HashMap<>();
            value.put(Keys.Name, currentUser.getName());
            value.put(Keys.Deleted, ServerValue.TIMESTAMP);

            currentThreadUser.setValue(value, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    emitter.onError(databaseError.toException());
                }
                else {
                    emitter.onComplete();
                }
            });
        }).subscribeOn(RX.io());
    }

}
