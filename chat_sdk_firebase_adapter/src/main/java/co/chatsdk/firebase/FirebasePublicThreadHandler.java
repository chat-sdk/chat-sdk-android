package co.chatsdk.firebase;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.dao.DaoCore;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import co.chatsdk.core.handlers.PublicThreadHandler;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebasePublicThreadHandler implements PublicThreadHandler {

    public Single<Thread> createPublicThreadWithName(final String name) {
        return createPublicThreadWithName(name, null);
    }

    public Single<Thread> createPublicThreadWithName(final String name, final String entityID) {
        return Single.create(new SingleOnSubscribe<Thread>() {
            @Override
            public void subscribe(final SingleEmitter<Thread> e) throws Exception {

                // Crating the new thread.
                // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
                final Thread thread = new Thread();

                User currentUser = NM.currentUser();
                thread.setCreator(currentUser);
                thread.setCreatorEntityId(currentUser.getEntityID());
                thread.setType(ThreadType.PublicGroup);
                thread.setName(name);
                thread.setEntityID(entityID);

                // Add the path and API key
                // This allows you to restrict public threads to a particular
                // API key or root key
                thread.setRootKey(Defines.RootPath);
                thread.setApiKey("");

                // Save the entity to the local db.
                DaoCore.createEntity(thread);

                ThreadWrapper wrapper = new ThreadWrapper(thread);

                wrapper.push().doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        DaoCore.updateEntity(thread);

                        // Add the thread to the list of public threads
                        DatabaseReference publicThreadRef = FirebasePaths.publicThreadsRef()
                                .child(thread.getEntityID());

                        HashMap<String, Object> value = new HashMap<>();
                        value.put(Keys.Null, "");

                        publicThreadRef.setValue(value, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError == null) {
                                    e.onSuccess(thread);
                                }
                                else {
                                    DaoCore.deleteEntity(thread);
                                    e.onError(databaseError.toException());
                                }
                            }
                        });
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        DaoCore.deleteEntity(thread);
                        e.onError(throwable);
                    }
                }).subscribe();
            }
        });
    }
}
