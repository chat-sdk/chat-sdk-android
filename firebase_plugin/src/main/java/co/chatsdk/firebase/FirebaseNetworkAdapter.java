package co.chatsdk.firebase;

import co.chatsdk.core.AbstractNetworkAdapter;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseNetworkAdapter extends AbstractNetworkAdapter {

    public FirebaseNetworkAdapter () {
        core = new FirebaseCorehandler();
    }

}
