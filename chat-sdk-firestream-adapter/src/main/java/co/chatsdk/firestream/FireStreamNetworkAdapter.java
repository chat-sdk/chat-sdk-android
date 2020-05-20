package co.chatsdk.firestream;

import co.chatsdk.firebase.FirebaseNetworkAdapter;

public class FireStreamNetworkAdapter extends FirebaseNetworkAdapter {

    public FireStreamNetworkAdapter() {

        events = new FirestreamEventHandler();
        thread = new FirestreamThreadHandler();
        blocking = new FirestreamBlockingHandler();
        contact = new FirestreamContactHandler();

    }

}
