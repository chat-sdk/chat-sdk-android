package com.braunster.chatsdk.interfaces;

import java.lang.reflect.Array;
import java.util.Map;

/**
 * Created by Erk on 26.07.2016.
 */
public interface BPushHandler {


    public void subscribeToPushChannel(String channel);
    public void unsubscribeToPushChannel(String channel);

    public void pushToChannels(Array channels, Map data);
}
