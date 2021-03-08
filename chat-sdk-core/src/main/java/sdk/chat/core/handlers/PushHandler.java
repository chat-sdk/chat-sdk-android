package sdk.chat.core.handlers;

import java.util.Map;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;
import sdk.chat.core.push.BroadcastHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PushHandler {

    Completable subscribeToPushChannel(String channel);
    Completable unsubscribeToPushChannel(String channel);

    Map<String, Object> pushDataForMessage(Message message);
    void sendPushNotification (Map<String, Object> data);

    BroadcastHandler getBroadcastHandler();
    void setBroadcastHandler(BroadcastHandler broadcastHandler);

    boolean enabled();

}
