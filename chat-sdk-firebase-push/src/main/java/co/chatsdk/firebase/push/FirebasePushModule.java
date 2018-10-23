package co.chatsdk.firebase.push;

import co.chatsdk.core.session.NetworkManager;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activate () {
        NetworkManager.shared().a.push = new FirebasePushHandler();
    }

}