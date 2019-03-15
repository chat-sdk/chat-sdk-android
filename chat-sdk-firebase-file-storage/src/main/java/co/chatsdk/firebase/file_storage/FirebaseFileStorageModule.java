package co.chatsdk.firebase.file_storage;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by ben on 9/1/17.
 */

public class FirebaseFileStorageModule {

    public static void activate () {
        ChatSDK.a().upload = new FirebaseUploadHandler();
    }

}
