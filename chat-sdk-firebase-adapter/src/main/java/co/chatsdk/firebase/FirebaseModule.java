package co.chatsdk.firebase;

import co.chatsdk.core.session.NetworkManager;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class FirebaseModule {
    public static void activate() {
        NetworkManager.shared().a = new FirebaseNetworkAdapter();
    }
}
