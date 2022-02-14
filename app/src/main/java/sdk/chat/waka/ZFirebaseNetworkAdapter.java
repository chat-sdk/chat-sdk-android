package sdk.chat.waka;

import sdk.chat.firebase.adapter.FirebaseAuthenticationHandler;
import sdk.chat.firebase.adapter.FirebaseContactHandler;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.chat.firebase.adapter.FirebaseNetworkAdapter;
import sdk.chat.firebase.adapter.FirebasePublicThreadHandler;
import sdk.chat.firebase.adapter.FirebaseSearchHandler;
import sdk.chat.firebase.adapter.FirebaseThreadHandler;

public class ZFirebaseNetworkAdapter extends FirebaseNetworkAdapter {

    public ZFirebaseNetworkAdapter () {
        events = new ZFirebaseEventHandler();
        core = new FirebaseCoreHandler();
        auth = new FirebaseAuthenticationHandler();
        thread = new FirebaseThreadHandler();
        publicThread = new FirebasePublicThreadHandler();
        search = new FirebaseSearchHandler();
        contact = new FirebaseContactHandler();
    }

}
