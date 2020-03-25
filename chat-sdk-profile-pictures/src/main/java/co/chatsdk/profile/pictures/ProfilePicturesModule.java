package co.chatsdk.profile.pictures;

import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;

/**
 * Created by Pepe on 01/12/19.
 */

public class ProfilePicturesModule implements Module {

    public static final ProfilePicturesModule instance = new ProfilePicturesModule();

    public static ProfilePicturesModule shared() {
        return instance;
    }

    public void activate () {
        ChatSDK.a().profilePictures = new BaseProfilePicturesHandler();
    }

    @Override
    public String getName() {
        return "ProfilePicturesModule";
    }

}
