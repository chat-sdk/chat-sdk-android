package co.chatsdk.xmpp.read_receipt;

import android.content.Context;

import sdk.chat.core.handlers.Module;
import sdk.chat.core.session.ChatSDK;

public class XMPPReadReceiptsModule implements Module {

    protected static final XMPPReadReceiptsModule instance = new XMPPReadReceiptsModule();

    public static XMPPReadReceiptsModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().readReceipts = new XMPPReadReceiptHandler();
    }

    @Override
    public String getName() {
        return "XMPPReadReceiptsModule";
    }
}
