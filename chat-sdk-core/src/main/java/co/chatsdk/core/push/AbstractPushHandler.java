package co.chatsdk.core.push;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.hook.AsyncExecutor;
import co.chatsdk.core.hook.Executor;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;
import co.chatsdk.core.dao.Thread;

public abstract class AbstractPushHandler implements PushHandler {

    public ChannelManager channelManager = new ChannelManager();

    public AbstractPushHandler() {

        // We need to subscribe / unsubscribe to all the necessary channels after login and logout
        ChatSDK.hook().addHook(Hook.sync(data -> {
            // Unsubscribe from all channels that aren't related to this user
            channelManager.channelsForUsersExcludingCurrent(this::unsubscribeToPushChannel);

            List<Completable> completables = new ArrayList<>();

            if (!channelManager.isSubscribed(ChatSDK.currentUserID())) {
                completables.add(subscribeToPushChannel(ChatSDK.currentUserID()));
            }

            for (Thread t: ChatSDK.db().allThreads()) {
                if (!channelManager.isSubscribed(t.getEntityID())) {
                    completables.add(subscribeToPushChannel(t.getEntityID()));
                }
            }

            Completable.merge(completables).subscribe(ChatSDK.events());

        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.async(data -> {
            return Completable.defer(() -> {
                ArrayList<Completable> completables = new ArrayList<>();

                // Unsubscribe the user
                completables.add(unsubscribeToPushChannel(ChatSDK.currentUserID()));

                // Unsubscribe from the threads
                for (Thread t: ChatSDK.db().allThreads()) {
                    completables.add(unsubscribeToPushChannel(t.getEntityID()));
                }
                return Completable.merge(completables);
            });

        }), HookEvent.WillLogout);

    }

    public static String QuickReplyNotificationCategory = "co.chatsdk.QuickReply";

    protected BroadcastHandler broadcastHandler = new BaseBroadcastHandler();

    public BroadcastHandler getBroadcastHandler() {
        return broadcastHandler;
    }

    public void setBroadcastHandler(BroadcastHandler broadcastHandler) {
        this.broadcastHandler = broadcastHandler;
    }

    @Override
    public HashMap<String, Object> pushDataForMessage(Message message) {
        String body = message.getText();

        if (body == null || body.isEmpty() || !ChatSDK.config().clientPushEnabled) {
            return null;
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
            return null;
        }

        HashMap<String, Object> data = new HashMap<>();

        // TODO: Parameterise this - update "threadId" in XMPP too
        data.put("userIds", users);
        data.put("body", body);
        data.put("type", message.getType());
        data.put("senderId", message.getSender().getEntityID());
        data.put("threadId", message.getThread().getEntityID());
        data.put("action", ChatSDK.config().pushNotificationAction != null ? ChatSDK.config().pushNotificationAction : QuickReplyNotificationCategory);
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().pushNotificationSound)) {
            data.put("sound", ChatSDK.config().pushNotificationSound);
        }

        return data;
    }

    @Override
    public Completable subscribeToPushChannel(String channel) {
        return Completable.create(emitter -> {
            channelManager.addChannel(channel);
            emitter.onComplete();
        });
    }

    @Override
    public Completable unsubscribeToPushChannel(String channel) {
        return Completable.create(emitter -> {
            channelManager.removeChannel(channel);
            emitter.onComplete();
        });
    }

}
