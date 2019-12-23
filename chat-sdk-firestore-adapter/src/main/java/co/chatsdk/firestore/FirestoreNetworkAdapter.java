package co.chatsdk.firestore;

import co.chatsdk.firebase.FirebaseNetworkAdapter;
import sdk.chat.micro.Fireflyy;

public class FirestoreNetworkAdapter extends FirebaseNetworkAdapter {

    public FirestoreNetworkAdapter () {

        Fireflyy.shared().initialize();

        events = new FirestoreEventHandler();
        thread = new FirestoreThreadHandler();
//        typingIndicator = new FirestoreTypingIndicatorHandler();
        readReceipts = new FirestoreReadReceiptHandler();
        blocking = new FirestoreBlockingHandler();
        contact = new FirestoreContactHandler();

    }

}
