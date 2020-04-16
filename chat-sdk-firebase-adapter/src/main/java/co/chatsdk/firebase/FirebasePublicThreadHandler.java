package co.chatsdk.firebase;


import java.util.HashMap;

import sdk.chat.core.base.AbstractPublicThreadHandler;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.firebase.wrappers.ThreadPusher;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class FirebasePublicThreadHandler extends AbstractPublicThreadHandler {

    public Single<Thread> createPublicThreadWithName(final String name, final String entityID, HashMap<String, String> meta, String imageURL) {
        return Single.create((SingleOnSubscribe<ThreadPusher>) emitter -> {
            // If the entity ID is set, see if the thread exists and return it if it does
            if (entityID != null) {
                Thread t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    emitter.onSuccess(new ThreadPusher(t, false));
                    return;
                }
            }

            // Creating the new thread.
            // This thread would not be saved to the local db until it is successfully uploaded to the firebase server.
            final Thread thread = ChatSDK.db().createEntity(Thread.class);

            User currentUser = ChatSDK.currentUser();
            thread.setCreator(currentUser);
            thread.setType(ThreadType.PublicGroup);
            thread.setName(name);
            thread.setEntityID(entityID);
            thread.setImageUrl(imageURL);

            thread.update();

            if (meta != null) {
                thread.updateValues(meta);
            }
            emitter.onSuccess(new ThreadPusher(thread, true));

        }).flatMap((Function<ThreadPusher, SingleSource<Thread>>) ThreadPusher::push)
                .subscribeOn(RX.io());
    }
}
