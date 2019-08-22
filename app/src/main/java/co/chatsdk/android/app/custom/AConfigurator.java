package co.chatsdk.android.app.custom;

import co.chatsdk.core.session.ChatSDK;

public class AConfigurator {

    public static void configure () {
        ChatSDK.ui().setLoginActivity(ALoginActivity.class);
        ChatSDK.ui().setChatActivity(AChatActivity.class);
        ChatSDK.ui().setProfileActivity(AProfileActivity.class);

        ChatSDK.ui().setPrivateThreadsFragment(new APrivateThreadsFragment());
        ChatSDK.ui().setPublicThreadsFragment(new APublicThreadsFragment());
        ChatSDK.ui().setContactsFragment(new AContactsFragment());

        ChatSDK.ui().setProfileFragmentProvider(AProfileFragment::newInstance);
    }

}
