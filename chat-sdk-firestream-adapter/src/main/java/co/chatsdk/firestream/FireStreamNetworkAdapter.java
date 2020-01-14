package co.chatsdk.firestream;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import firestream.chat.Config;
import firestream.chat.namespace.Fire;

public class FireStreamNetworkAdapter extends FirebaseNetworkAdapter {

    public FireStreamNetworkAdapter() {

        if (!Fire.stream().isInitialized()) {
            Config config = new Config();
            try {
                config.setRoot(ChatSDK.config().firebaseRootPath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Fire.stream().initialize(ChatSDK.shared().context(), config);
        }

        events = new FirestreamEventHandler();
        thread = new FirestreamThreadHandler();
//        typingIndicator = new FirestreamTypingIndicatorHandler();
        readReceipts = new FirestreamReadReceiptHandler();
        blocking = new FirestreamBlockingHandler();
        contact = new FirestreamContactHandler();

    }

}
