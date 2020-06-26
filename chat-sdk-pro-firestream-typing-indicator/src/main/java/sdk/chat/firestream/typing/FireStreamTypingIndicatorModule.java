package sdk.chat.firestream.typing;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;

public class FireStreamTypingIndicatorModule extends AbstractModule {

    public static final FireStreamTypingIndicatorModule instance = new FireStreamTypingIndicatorModule();

    public static FireStreamTypingIndicatorModule shared() {
        return instance;
    }

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().typingIndicator = new FirestreamTypingIndicatorHandler();
    }

    @Override
    public String getName() {
        return "FireStreamTypingIndicatorModule";
    }

    @Override
    public void stop() {

    }
}
