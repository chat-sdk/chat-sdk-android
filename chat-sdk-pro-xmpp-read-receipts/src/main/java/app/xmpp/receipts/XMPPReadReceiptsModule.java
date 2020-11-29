package app.xmpp.receipts;

import android.content.Context;

import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.licensing.Report;

public class XMPPReadReceiptsModule extends AbstractModule {

    protected static final XMPPReadReceiptsModule instance = new XMPPReadReceiptsModule();

    public static XMPPReadReceiptsModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        Report.shared().add(getName());
        ChatSDK.a().readReceipts = new XMPPReadReceiptHandler();
    }

    @Override
    public String getName() {
        return "XMPPReadReceiptsModule";
    }

    @Override
    public void stop() {

    }
}
