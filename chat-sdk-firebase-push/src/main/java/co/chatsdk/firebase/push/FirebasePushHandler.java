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
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler implements PushHandler {

    public static String QuickReplyNotificationCategory = "co.chatsdk.QuickReply";

    protected BroadcastHandler broadcastHandler = new BaseBroadcastHandler();

    public FirebasePushHandler () {

    }

    public BroadcastHandler getBroadcastHandler() {
        return broadcastHandler;
    }

    public void setBroadcastHandler(BroadcastHandler broadcastHandler) {
        this.broadcastHandler = broadcastHandler;
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
    public void pushForMessage (Message message) {

        String body = message.getText();

        if (body == null || body.isEmpty() || !ChatSDK.config().clientPushEnabled) {
            return;
        }

        HashMap<String, String> users = new HashMap<>();
        for(User user : message.getThread().getUsers()) {

            String userName = user.getName();
            String userEntityID = user.getPushChannel();

            if (!user.isMe() && !StringChecker.isNullOrEmpty(userEntityID) && !StringChecker.isNullOrEmpty(userName)) {
                if(!user.getIsOnline() || !ChatSDK.config().onlySendPushToOfflineUsers) {
                    users.put(userEntityID, userName);
                }
            }
        }

        if(users.keySet().size() == 0) {
            return;
        }

        HashMap<String, Object> data = new HashMap<>();

        data.put("userIds", users);
        data.put("body", body);
        data.put("type", message.getType());
        data.put("senderId", message.getSender().getEntityID());
        data.put("threadId", message.getThread().getEntityID());
        data.put("action", ChatSDK.config().pushNotificationAction != null ? ChatSDK.config().pushNotificationAction : QuickReplyNotificationCategory);
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().pushNotificationSound)) {
            data.put("sound", ChatSDK.config().pushNotificationSound);
        }

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
