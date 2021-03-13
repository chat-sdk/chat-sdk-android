package sdk.chat.firestream.blocking;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.licensing.Report;

public class FireStreamBlockingModule extends AbstractModule {

    public static final FireStreamBlockingModule instance = new FireStreamBlockingModule();

    public static FireStreamBlockingModule shared() {
        return instance;
    }

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().blocking = new FirestreamBlockingHandler();
        Report.shared().add(getName());
    }

}
