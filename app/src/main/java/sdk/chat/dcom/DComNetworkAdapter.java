package sdk.chat.dcom;

import sdk.chat.firebase.adapter.FirebaseAuthenticationHandler;
import sdk.chat.firebase.adapter.FirebaseContactHandler;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.chat.firebase.adapter.FirebaseEventHandler;
import sdk.chat.firebase.adapter.FirebaseNetworkAdapter;
import sdk.chat.firebase.adapter.FirebasePublicThreadHandler;
import sdk.chat.firebase.adapter.FirebaseSearchHandler;

public class DComFirebaseNetworkAdapter extends FirebaseNetworkAdapter {

    public DComFirebaseNetworkAdapter () {
        events = new FirebaseEventHandler();
        core = new FirebaseCoreHandler();
        auth = new FirebaseAuthenticationHandler();
        thread = new DComThreadHandler();
        publicThread = new FirebasePublicThreadHandler();
        search = new FirebaseSearchHandler();
        contact = new FirebaseContactHandler();
    }

}
