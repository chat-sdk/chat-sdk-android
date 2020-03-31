package co.chatsdk.typing_indicator;


import android.content.Context;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;

/**
 * Created by ben on 10/5/17.
 */

public class FirebaseTypingIndicatorModule implements Module {

    public static final FirebaseTypingIndicatorModule instance = new FirebaseTypingIndicatorModule();

    public static FirebaseTypingIndicatorModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().typingIndicator = new FirebaseTypingIndicatorHandler();
    }

    @Override
    public String getName() {
        return "FirebaseTypingIndicatorModule";
    }
}
