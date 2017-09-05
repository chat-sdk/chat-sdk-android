package co.chatsdk.firebase.push;

import org.json.JSONObject;

import java.util.Collection;

import co.chatsdk.core.handlers.PushHandler;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler implements PushHandler {

    public FirebasePushHandler () {

    }

    @Override
    public boolean subscribeToPushChannel(String channel) {
        return false;
    }

    @Override
    public boolean unsubscribeToPushChannel(String channel) {
        return false;
    }

    @Override
    public void pushToChannels(Collection<String> channels, JSONObject data) {

    }
}
