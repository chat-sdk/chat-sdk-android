package co.chatsdk.firestore;

import co.chatsdk.firebase.FirebaseNetworkAdapter;
import sdk.chat.micro.MicroChatSDK;

public class FirestoreNetworkAdapter extends FirebaseNetworkAdapter {

    public FirestoreNetworkAdapter () {

        MicroChatSDK.shared().initialize();

        events = new FirestoreEventHandler();
        thread = new FirestoreThreadHandler();
//        typingIndicator = new FirestoreTypingIndicatorHandler();
        readReceipts = new FirestoreReadReceiptHandler();

    }

}
