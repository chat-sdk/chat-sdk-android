package co.chatsdk.firebase.push;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.push.AbstractPushHandler;
import co.chatsdk.core.push.BaseBroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler extends AbstractPushHandler {

    public FirebasePushHandler () {

    }

    @Override
    public void subscribeToPushChannel(String channel) {
        FirebaseMessaging.getInstance().subscribeToTopic(channel);
    }

    @Override
    public void unsubscribeToPushChannel(String channel) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(channel);
    }

    @Override
    public void sendPushNotification (HashMap<String, Object> data) {
        if (data != null) {
            FirebaseFunctions.getInstance().getHttpsCallable("pushToChannels").call(data).continueWith((Continuation<HttpsCallableResult, String>) task -> {
                if(task.getException() != null) {
                    Timber.d(task.getException());
                }
                else {
                    Timber.d(task.getResult().getData().toString());
                }
                return null;
            });
        }
    }

}
