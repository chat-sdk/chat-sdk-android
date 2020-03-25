package co.chatsdk.firestream;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import firestream.chat.FirestreamConfig;
import firestream.chat.namespace.Fire;

public class FireStreamNetworkAdapter extends FirebaseNetworkAdapter {

    public FireStreamNetworkAdapter() {

        events = new FirestreamEventHandler();
        thread = new FirestreamThreadHandler();
//        typingIndicator = new FirestreamTypingIndicatorHandler();
        readReceipts = new FirestreamReadReceiptHandler();
        blocking = new FirestreamBlockingHandler();
        contact = new FirestreamContactHandler();

    }

}
