package co.chatsdk.firestore;

import co.chatsdk.firebase.FirebaseNetworkAdapter;
import sdk.chat.micro.Fireflyy;
import sdk.chat.micro.namespace.Fire;
import sdk.chat.micro.namespace.Fly;

public class FirestoreNetworkAdapter extends FirebaseNetworkAdapter {

    public FirestoreNetworkAdapter () {

        Fly.y.initialize();

        events = new FirestoreEventHandler();
        thread = new FirestoreThreadHandler();
//        typingIndicator = new FirestoreTypingIndicatorHandler();
        readReceipts = new FirestoreReadReceiptHandler();
        blocking = new FirestoreBlockingHandler();
        contact = new FirestoreContactHandler();

    }

}
