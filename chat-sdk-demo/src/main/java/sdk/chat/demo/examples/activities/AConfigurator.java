package sdk.chat.demo.examples.activities;

import sdk.chat.core.session.ChatSDK;

public class AConfigurator {

    public static void configure () {
        ChatSDK.ui().setLoginActivity(ALoginActivity.class);
        ChatSDK.ui().setChatActivity(AChatActivity.class);
        ChatSDK.ui().setProfileActivity(AProfileActivity.class);

        ChatSDK.ui().setPrivateThreadsFragment(new APrivateThreadsFragment());
        ChatSDK.ui().setPublicThreadsFragment(new APublicThreadsFragment());
        ChatSDK.ui().setContactsFragment(new AContactsFragment());

        ChatSDK.ui().setProfileFragmentProvider(user -> {
            AProfileFragment fragment = new AProfileFragment();
            fragment.setUser(user);
            return fragment;
        });
    }

}
