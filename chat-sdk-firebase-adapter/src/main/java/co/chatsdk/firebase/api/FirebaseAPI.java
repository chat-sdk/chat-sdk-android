package co.chatsdk.firebase.api;

public class FirebaseAPI {

    protected static final FirebaseAPI shared = new FirebaseAPI();
    protected AbstractFirebaseAdapter adapter;

    public static FirebaseAPI shared () {
        return shared;
    }

    public AbstractFirebaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(AbstractFirebaseAdapter adapter) {
        this.adapter = adapter;
    }

}
