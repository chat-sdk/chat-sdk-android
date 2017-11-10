package co.chatsdk.firebase.push;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.session.NetworkManager;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activateForFirebase () {
        FirebasePushModule.activate(new FirebasePushHandler.TokenPusher() {
            @Override
            public void pushToken() {
                NM.core().pushUser().doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }).subscribe();
            }
        });
    }

    public static void activate (FirebasePushHandler.TokenPusher pusher) {
        NetworkManager.shared().a.push = new FirebasePushHandler(pusher);
    }

}