package co.chatsdk.firebase.blocking;

import android.content.Context;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;

/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseBlockingModule implements Module {

    public static final FirebaseBlockingModule instance = new FirebaseBlockingModule();
    public static FirebaseBlockingModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().blocking = new FirebaseBlockingHandler();
    }

    @Override
    public String getName() {
        return "FirebaseBlockingModule";
    }
}
