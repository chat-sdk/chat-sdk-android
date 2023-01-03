package sdk.chat.firestream.adapter;

import sdk.chat.firebase.adapter.FirebaseAuthenticationHandler;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.chat.firebase.adapter.FirebaseNetworkAdapter;
import sdk.chat.firebase.adapter.FirebasePublicThreadHandler;
import sdk.chat.firebase.adapter.FirebaseSearchHandler;

public class FireStreamNetworkAdapter extends FirebaseNetworkAdapter {

    public FireStreamNetworkAdapter() {
        events = new FirestreamEventHandler();
        core = new FirebaseCoreHandler();
        auth = new FirebaseAuthenticationHandler();
        thread = new FirestreamThreadHandler();
        publicThread = new FirebasePublicThreadHandler();
        search = new FirebaseSearchHandler();
        contact = new FirestreamContactHandler();
    }

}
