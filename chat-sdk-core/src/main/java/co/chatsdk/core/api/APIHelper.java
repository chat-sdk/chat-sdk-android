package co.chatsdk.core.api;

import android.app.Activity;
import android.content.Context;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import co.chatsdk.core.dao.Thread;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class APIHelper {

    /**
     * Create a private 1-to-1 chat with another user given their entity id
     * @param userEntityID
     * @return
     */
    public Single<Thread> createPrivateChatWithUser (String userEntityID) {
        return ChatSDK.thread().createThread("", ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEntityID));
    }

    /**
     * Create a private 1-to-1 chat with another user given their entity id
     * and then start a new chat activity. If there is an error pass it back
     * as a completable
     * @param userEntityID
     * @return
     */
    public Completable startPrivateChatActivityWithUser (Context context, String userEntityID) {
        return createPrivateChatWithUser(userEntityID).doOnSuccess(thread -> {
            // Start the chat activity
            ChatSDK.ui().startChatActivityForID(context, userEntityID);
        }).ignoreElement();
    }


}
