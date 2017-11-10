package co.chatsdk.firebase;

import android.content.Context;

import co.chatsdk.core.session.NetworkManager;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class FirebaseModule {
    public static void activate(Context context) {
        NetworkManager.shared().a = new FirebaseNetworkAdapter();
    }
}
