package co.chatsdk.core.handlers;

import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface PushHandler {

    void subscribeToPushChannel(String channel);
    void unsubscribeToPushChannel(String channel);

    void pushToChannels(List<String> channels, Map<String, String> data);
    void pushToUsers (List<User> users, Message message);

}
