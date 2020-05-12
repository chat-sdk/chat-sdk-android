package co.chatsdk.firebase.wrappers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.firebase.moderation.Permission;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Single;
import io.reactivex.SingleSource;

/**
 * This class is a helper class. When we are creating a thread sometimes we want to create it
 * but sometimes we want to return a pre-existing thread. This class helps us with the RX stream
 * to know when to actually create the thread or when to just return a pre-existing thread
 */
public class ThreadPusher {

    protected boolean push;
    protected Thread thread;

    public ThreadPusher (Thread thread, boolean push) {
        this.thread = thread;
        this.push = push;
    }

    public Single<Thread> push() {
        return Single.defer((Callable<SingleSource<Thread>>) () -> {
            if (push) {
                if (thread.typeIs(ThreadType.Public)) {
                    return new ThreadWrapper(thread).push()
                            .doOnError(throwable -> {
                                thread.delete();
                            })
                            .andThen((SingleSource<Thread>) observer -> {
                                thread.update();
                                // Add the thread to the list of public threads
                                DatabaseReference publicThreadRef = FirebasePaths.publicThreadsRef()
                                        .child(thread.getEntityID());

                                HashMap<String, Object> value = new HashMap<>();
                                value.put(Keys.CreationDate, ServerValue.TIMESTAMP);

                                publicThreadRef.setValue(value, (databaseError, databaseReference) -> {
                                    if (databaseError == null) {
                                        observer.onSuccess(thread);
                                    } else {
                                        thread.delete();
                                        observer.onError(databaseError.toException());
                                    }
                                });
                            });
                } else {
                    ThreadWrapper wrapper = new ThreadWrapper(thread);
                    return wrapper.push()
                            .doOnError(throwable -> {
                                thread.delete();
                            })
                            .andThen(Completable.defer(() -> {
                                return ChatSDK.thread().addUsersToThread(thread, thread.getUsers());
                            }))
                            .toSingle(() -> thread);
                }
            } else {
                return Single.just(thread);
            }
        });
    }
}


