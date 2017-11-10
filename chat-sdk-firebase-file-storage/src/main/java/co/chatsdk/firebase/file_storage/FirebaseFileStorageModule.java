package co.chatsdk.firebase.file_storage;

import co.chatsdk.core.session.NetworkManager;

/**
 * Created by ben on 9/1/17.
 */

public class FirebaseFileStorageModule {

    public static void activate () {
        NetworkManager.shared().a.upload = new FirebaseUploadHandler();
    }

}
