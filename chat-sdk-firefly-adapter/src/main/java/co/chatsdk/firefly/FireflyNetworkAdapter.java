package co.chatsdk.firefly;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import firefly.sdk.chat.Config;
import firefly.sdk.chat.namespace.Fl;

public class FireflyNetworkAdapter extends FirebaseNetworkAdapter {

    public FireflyNetworkAdapter() {

        if (!Fl.y.isInitialized()) {
            Config config = new Config();
            config.root = ChatSDK.config().firebaseRootPath;
            config.sandbox = "firefly";
            Fl.y.initialize(ChatSDK.shared().context(), config);
        }

        events = new FireflyEventHandler();
        thread = new FireflyThreadHandler();
//        typingIndicator = new FireflyTypingIndicatorHandler();
        readReceipts = new FireflyReadReceiptHandler();
        blocking = new FireflyBlockingHandler();
        contact = new FireflyContactHandler();

    }

}
