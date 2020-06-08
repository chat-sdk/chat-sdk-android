package sdk.chat.firestream.adapter;

import sdk.chat.firebase.adapter.FirebaseNetworkAdapter;

public class FireStreamNetworkAdapter extends FirebaseNetworkAdapter {

    public FireStreamNetworkAdapter() {
        events = new FirestreamEventHandler();
        thread = new FirestreamThreadHandler();
        contact = new FirestreamContactHandler();
    }

}
