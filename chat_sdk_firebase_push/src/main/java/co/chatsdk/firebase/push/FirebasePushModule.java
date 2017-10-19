package co.chatsdk.firebase.push;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.session.NetworkManager;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activateForFirebase () {
        FirebasePushModule.activate(new FirebasePushHandler.TokenPusher() {
            @Override
            public void pushToken() {
                NM.core().pushUser();
            }
        });
    }

    public static void activate (FirebasePushHandler.TokenPusher pusher) {
        NetworkManager.shared().a.push = new FirebasePushHandler(pusher);
    }

}