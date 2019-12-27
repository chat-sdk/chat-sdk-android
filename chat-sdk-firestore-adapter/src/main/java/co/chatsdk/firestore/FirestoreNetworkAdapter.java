package co.chatsdk.firestore;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import firefly.sdk.chat.Config;
import firefly.sdk.chat.namespace.Fl;

public class FirestoreNetworkAdapter extends FirebaseNetworkAdapter {

    public FirestoreNetworkAdapter () {

        Config config = new Config();
        config.database = Config.DatabaseType.Realtime;
        config.root = ChatSDK.config().firebaseRootPath;
        config.sandbox = "firefly";

        Fl.y.initialize(ChatSDK.shared().context(), config);

        events = new FirestoreEventHandler();
        thread = new FirestoreThreadHandler();
//        typingIndicator = new FirestoreTypingIndicatorHandler();
        readReceipts = new FirestoreReadReceiptHandler();
        blocking = new FirestoreBlockingHandler();
        contact = new FirestoreContactHandler();

    }

}
