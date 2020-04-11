package co.chatsdk.ejabberd.file_storage;

import sdk.chat.core.session.ChatSDK;

public class EjabberdFileStorageModule {
    public static void activate () {
        ChatSDK.a().upload = new EjabberdUploadHandler();
    }
}
