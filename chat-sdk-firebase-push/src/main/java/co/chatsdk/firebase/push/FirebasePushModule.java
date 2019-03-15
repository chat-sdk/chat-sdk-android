package co.chatsdk.firebase.push;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushModule  {

    public static void activate () {
        ChatSDK.shared().a().push = new FirebasePushHandler();
    }

}