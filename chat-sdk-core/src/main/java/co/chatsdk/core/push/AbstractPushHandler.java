package co.chatsdk.core.push;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;

public abstract class AbstractPushHandler implements PushHandler {

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


}
