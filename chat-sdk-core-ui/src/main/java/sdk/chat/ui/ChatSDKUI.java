package sdk.chat.ui;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.ProfileFragmentProvider;
import sdk.chat.ui.activities.AddUsersToThreadActivity;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.activities.CreateThreadActivity;
import sdk.chat.ui.activities.EditProfileActivity;
import sdk.chat.ui.activities.EditThreadActivity;
import sdk.chat.ui.activities.ForwardMessageActivity;
import sdk.chat.ui.activities.LoginActivity;
import sdk.chat.ui.activities.MainActivity;
import sdk.chat.ui.activities.PostRegistrationActivity;
import sdk.chat.ui.activities.ProfileActivity;
import sdk.chat.ui.activities.SearchActivity;
import sdk.chat.ui.activities.SplashScreenActivity;
import sdk.chat.ui.activities.ThreadDetailsActivity;
import sdk.chat.ui.fragments.ContactsFragment;
import sdk.chat.ui.fragments.PrivateThreadsFragment;
import sdk.chat.ui.fragments.PublicThreadsFragment;

public class ChatSDKUI {

    public static void setLoginActivity(Class<? extends LoginActivity> loginActivity) {
        ChatSDK.ui().setLoginActivity(loginActivity);
    }

    public static void setSplashScreenActivity(Class<? extends SplashScreenActivity> splashScreenActivity) {
        ChatSDK.ui().setSplashScreenActivity(splashScreenActivity);
    }

    public static void setMainActivity(Class<? extends MainActivity> mainActivity) {
        ChatSDK.ui().setMainActivity(mainActivity);
    }

    public static void setChatActivity(Class<? extends ChatActivity> chatActivity) {
        ChatSDK.ui().setChatActivity(chatActivity);
    }

    public static void setThreadDetailsActivity(Class<? extends ThreadDetailsActivity> threadDetailsActivity) {
        ChatSDK.ui().setThreadDetailsActivity(threadDetailsActivity);
    }

    public static void setEditThreadActivity(Class<? extends EditThreadActivity> editThreadActivity) {
        ChatSDK.ui().setEditThreadActivity(editThreadActivity);
    }

    public static void setForwardMessageActivity(Class<? extends ForwardMessageActivity> forwardMessageActivity) {
        ChatSDK.ui().setForwardMessageActivity(forwardMessageActivity);
    }

    public static void setAddUsersToThreadActivity(Class<? extends AddUsersToThreadActivity> addUsersToThreadActivity) {
        ChatSDK.ui().setAddUsersToThreadActivity(addUsersToThreadActivity);
    }

    public static void setCreateThreadActivity(Class<? extends CreateThreadActivity> createThreadActivity) {
        ChatSDK.ui().setCreateThreadActivity(createThreadActivity);
    }

    public static void setSearchActivity(Class<? extends SearchActivity> searchActivity) {
        ChatSDK.ui().setSearchActivity(searchActivity);
    }

    public static void setEditProfileActivity(Class<? extends EditProfileActivity> editProfileActivity) {
        ChatSDK.ui().setEditProfileActivity(editProfileActivity);
    }

    public static void setProfileActivity(Class<? extends ProfileActivity> profileActivity) {
        ChatSDK.ui().setProfileActivity(profileActivity);
    }

    public static void setPostRegistrationActivity (Class<? extends PostRegistrationActivity> postRegistrationActivity) {
        ChatSDK.ui().setPostRegistrationActivity(postRegistrationActivity);
    }

    public static void setPrivateThreadsFragment(PrivateThreadsFragment privateThreadsFragment) {
        ChatSDK.ui().setPrivateThreadsFragment(privateThreadsFragment);
    }

    public static void setPublicThreadsFragment(PublicThreadsFragment publicThreadsFragment) {
        ChatSDK.ui().setPublicThreadsFragment(publicThreadsFragment);
    }

    public static void setContactsFragment(ContactsFragment contactsFragment) {
        ChatSDK.ui().setContactsFragment(contactsFragment);
    }

    public static void setProfileFragmentProvider(ProfileFragmentProvider profileFragmentProvider) {
        ChatSDK.ui().setProfileFragmentProvider(profileFragmentProvider);
    }

}
