package co.chatsdk.core.handlers;

import org.json.JSONObject;

import java.util.Collection;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PushHandler {

    public boolean subscribeToPushChannel(String channel);
    public boolean unsubscribeToPushChannel(String channel);

    public void pushToChannels(Collection<String> channels, JSONObject data);
    //public void pushForMesasge(Message message);


}
