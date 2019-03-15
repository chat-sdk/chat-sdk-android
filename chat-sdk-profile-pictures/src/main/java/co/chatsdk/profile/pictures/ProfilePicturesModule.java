package co.chatsdk.profile.pictures;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by Pepe on 01/12/19.
 */

public class ProfilePicturesModule {

    public static void activate () {
        ChatSDK.a().profilePictures = new BaseProfilePicturesHandler();
    }

}
