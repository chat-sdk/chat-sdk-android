package sdk.chat.firestream.blocking;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;

public class FirestreamBlockingModule extends AbstractModule {

    public static final FirestreamBlockingModule instance = new FirestreamBlockingModule();

    public static FirestreamBlockingModule shared() {
        return instance;
    }

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().blocking = new FirestreamBlockingHandler();
    }

    @Override
    public String getName() {
        return "FirestreamBlockingModule";
    }
}
