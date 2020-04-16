package sdk.chat.core.api;

import android.content.Context;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.guru.common.RX;
import sdk.chat.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

@Deprecated
public class APIHelper {

    /**
     * Create a private 1-to-1 chat with another user given their entity id
     * @param userEntityID
     * @return
     */
    public static Single<Thread> createPrivateChatWithUser (String userEntityID) {
        return ChatSDK.thread().createThread("", ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEntityID));
    }

    /**
     * Create a private 1-to-1 chat with another user given their entity id
     * and then start a new chat activity. If there is an error pass it back
     * as a completable
     * @param userEntityID
     * @return
     */
    public static Completable startPrivateChatActivityWithUser (Context context, String userEntityID) {
        return createPrivateChatWithUser(userEntityID).doOnSuccess(thread -> {
            // Start the chat activity
            ChatSDK.ui().startChatActivityForID(context, userEntityID);
        }).ignoreElement();
    }

    public static Single<User> fetchRemoteUser(String userEntityID) {
        return Single.create((SingleOnSubscribe<User>) emitter -> {
            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEntityID);
            emitter.onSuccess(user);
        }).subscribeOn(RX.db())
                .flatMap(user -> ChatSDK.core().userOn(user).toSingle(() -> user));
    }
}
