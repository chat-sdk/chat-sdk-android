package sdk.chat.firebase.adapter;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.base.AbstractPublicThreadHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebasePublicThreadHandler extends AbstractPublicThreadHandler {

    public Single<ThreadX> createPublicThreadWithName(final String name, final String entityID, Map<String, Object> meta, String imageURL) {
        return Single.defer(() -> {
            // If the entity ID is set, see if the thread exists and return it if it does
            if (entityID != null) {
                ThreadX t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    return Single.just(t);
                }
            }

            // Creating the new thread.
            // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
            final ThreadX thread = ChatSDK.db().createEntity(ThreadX.class);

            User currentUser = ChatSDK.currentUser();
            thread.setCreator(currentUser);
            thread.setType(ThreadType.PublicGroup);
            thread.setName(name);
            thread.setEntityID(entityID);
            thread.setImageUrl(imageURL);
            ChatSDK.db().update(thread);

            if (meta != null) {
                thread.updateValues(meta);
            }

            return FirebaseModule.config().provider.threadWrapper(thread).push()
                    .doOnError(throwable -> {
                        thread.cascadeDelete();
                    })
                    .andThen((SingleSource<ThreadX>) observer -> {
                        ChatSDK.db().update(thread);

                        // Add the thread to the list of public threads
                        DatabaseReference publicThreadRef = FirebasePaths.publicThreadsRef()
                                .child(thread.getEntityID());

                        HashMap<String, Object> value = new HashMap<>();
                        value.put(Keys.CreationDate, ServerValue.TIMESTAMP);

                        publicThreadRef.setValue(value, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                observer.onSuccess(thread);
                            } else {
                                thread.cascadeDelete();
                                observer.onError(databaseError.toException());
                            }
                        });
                    });
        }).subscribeOn(RX.db());
    }
}
