package co.chatsdk.firebase.push;

import android.net.Network;

import co.chatsdk.core.NetworkManager;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activate (FirebasePushHandler.TokenPusher pusher) {
        NetworkManager.shared().a.push = new FirebasePushHandler(pusher);
    }

}