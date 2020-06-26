package sdk.chat.firestream.receipts;

import android.content.Context;

import androidx.annotation.NonNull;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;

public class FireStreamReadReceiptsModule extends AbstractModule {

    public static final FireStreamReadReceiptsModule instance = new FireStreamReadReceiptsModule();

    public static FireStreamReadReceiptsModule shared() {
        return instance;
    }

    @Override
    public void activate(@NonNull Context context) {
        ChatSDK.a().readReceipts = new FirestreamReadReceiptHandler();
    }

    @Override
    public String getName() {
        return "FireStreamReadReceiptsModule";
    }

}
