package sdk.chat.core.push;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.PushHandler;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import io.reactivex.Completable;

public abstract class AbstractPushHandler implements PushHandler {

    public ChannelManager channelManager = new ChannelManager();

    public static String UserIds = "userIds";
    public static String Type = "type";
    public static String Body = "body";
    public static String SenderId = "senderId";
    public static String ThreadId = "threadId";
    public static String Action = "action";
    public static String Sound = "sound";

    public AbstractPushHandler() {

        // We need to subscribe / unsubscribe to all the necessary channels after login and logout
        ChatSDK.hook().addHook(Hook.sync(data -> {
            // Unsubscribe from all channels that aren't related to this user
            channelManager.channelsForUsersExcludingCurrent(this::unsubscribeToPushChannel);

            List<Completable> completables = new ArrayList<>();

            if (!channelManager.isSubscribed(ChatSDK.currentUserID())) {
                completables.add(subscribeToPushChannel(ChatSDK.currentUserID()));
            }

//            for (Thread t: ChatSDK.db().allThreads()) {
//                if (!channelManager.isSubscribed(t.getEntityID())) {
//                    completables.add(subscribeToPushChannel(t.getEntityID()));
//                }
//            }

            Completable.merge(completables).subscribe(ChatSDK.events());

        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.async(data -> {
            return Completable.defer(() -> {
                ArrayList<Completable> completables = new ArrayList<>();

                // Unsubscribe the user
                completables.add(unsubscribeToPushChannel(ChatSDK.currentUserID()));

                // Unsubscribe from the threads
//                for (Thread t: ChatSDK.db().allThreads()) {
//                    completables.add(unsubscribeToPushChannel(t.getEntityID()));
//                }
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

        data.put(UserIds, users);
        data.put(Body, body);
        data.put(Type, message.getType());
        data.put(SenderId, message.getSender().getEntityID());
        data.put(ThreadId, message.getThread().getEntityID());
        data.put(Action, ChatSDK.config().pushNotificationAction != null ? ChatSDK.config().pushNotificationAction : QuickReplyNotificationCategory);
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().pushNotificationSound)) {
            data.put(Sound, ChatSDK.config().pushNotificationSound);
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

    public String hashChannel(String channel) throws Exception {
        channel = channel.replace(".", "1");
        channel = channel.replace("%2E", "1");
        channel = channel.replace("@", "2");
        channel = channel.replace("%40", "2");
        channel = channel.replace(":", "3");
        channel = channel.replace("%3A", "3");
        return channel;
    }

    public String md5(String channel) throws NoSuchAlgorithmException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(channel.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public boolean enabled() {
        return true;
    }

}
