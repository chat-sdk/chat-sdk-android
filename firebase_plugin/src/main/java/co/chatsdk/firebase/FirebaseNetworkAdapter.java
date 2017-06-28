package co.chatsdk.firebase;

import co.chatsdk.core.AbstractNetworkAdapter;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseNetworkAdapter extends AbstractNetworkAdapter {

    public FirebaseNetworkAdapter () {
        core = new FirebaseCoreHandler();
        auth = new FirebaseAuthenticationHandler();
        thread = new FirebaseThreadHandler();
        upload = new FirebaseUploadHandler();
        events = FirebaseEventHandler.shared();
        publicThread = new FirebasePublicThreadHandler();
        search = new FirebaseSearchHandler();

    }

}
