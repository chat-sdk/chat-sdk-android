package co.chatsdk.firebase.blocking;

import co.chatsdk.core.session.NetworkManager;

/**
 * Created by pepe on 08.03.18.
 */

public class BlockingModule {

    public static void activate () {
        NetworkManager.shared().a.blocking = new FirebaseBlockingHandler();
    }

}
