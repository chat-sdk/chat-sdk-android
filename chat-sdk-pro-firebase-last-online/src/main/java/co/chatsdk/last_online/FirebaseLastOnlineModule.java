package co.chatsdk.last_online;

import android.content.Context;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;

public class FirebaseLastOnlineModule implements Module {

    public static final FirebaseLastOnlineModule instance = new FirebaseLastOnlineModule();
    public static FirebaseLastOnlineModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().lastOnline = new FirebaseLastOnlineHandler();
    }

    @Override
    public String getName() {
        return null;
    }
}
