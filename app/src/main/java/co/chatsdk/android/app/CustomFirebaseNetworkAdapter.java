package co.chatsdk.android.app;

import co.chatsdk.firebase.FirebaseNetworkAdapter;

public class CustomFirebaseNetworkAdapter extends FirebaseNetworkAdapter {

    public CustomFirebaseNetworkAdapter () {
        super();
        thread = new CustomFirebaseThreadHandler();
    }

}
