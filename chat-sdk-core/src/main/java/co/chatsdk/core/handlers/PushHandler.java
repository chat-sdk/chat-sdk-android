package co.chatsdk.core.handlers;

import java.util.HashMap;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.BroadcastHandler;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PushHandler {

    Completable subscribeToPushChannel(String channel);
    Completable unsubscribeToPushChannel(String channel);

    HashMap<String, Object> pushDataForMessage(Message message);
    void sendPushNotification (HashMap<String, Object> data);

    BroadcastHandler getBroadcastHandler();
    void setBroadcastHandler(BroadcastHandler broadcastHandler);
}
