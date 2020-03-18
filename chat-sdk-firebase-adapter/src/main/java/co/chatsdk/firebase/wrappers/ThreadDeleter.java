package co.chatsdk.firebase.wrappers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.schedulers.Schedulers;

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
        }).subscribeOn(Schedulers.io());
    }

    public Completable deleteMessages() {
        return Completable.create(emitter -> {
            List<Message> messages = new ArrayList<>(thread.getMessages());

            for (Message m : messages) {
                thread.removeMessage(m);
                m.delete();
            }
            thread.update();
            thread.refresh();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
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
        }).subscribeOn(Schedulers.io());
    }

}
