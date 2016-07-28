package com.braunster.chatsdk.interfaces;

import org.json.JSONObject;

import java.util.Collection;

/**
 * Created by Erk on 26.07.2016.
 */
public interface BPushHandler {


    public boolean subscribeToPushChannel(String channel);
    public boolean unsubscribeToPushChannel(String channel);

    public void pushToChannels(Collection<String> channels, JSONObject data);
}
