package com.braunster.androidchatsdk.firebaseplugin.firebase;

/**
 * Created by braunster on 11.06.15.
 */
public interface Pusher {

    void subscribeToPushChannel(String channel);

    void unsubscribeToPushChannel(String channel);
}
