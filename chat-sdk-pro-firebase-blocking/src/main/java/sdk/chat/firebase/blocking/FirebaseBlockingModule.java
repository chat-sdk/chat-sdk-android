package sdk.chat.firebase.blocking;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.licensing.Report;

/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseBlockingModule extends AbstractModule {

    public static final FirebaseBlockingModule instance = new FirebaseBlockingModule();
    public static FirebaseBlockingModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().blocking = new FirebaseBlockingHandler();
        Report.shared().add(getName());
    }

    @Override
    public void stop() {

    }
}
