package sdk.chat.ui;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.ProfileFragmentProvider;
import sdk.chat.core.utils.ProfileOption;
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
import sdk.chat.ui.activities.thread.details.ThreadDetailsActivity;
import sdk.chat.ui.custom.MessageRegistrationManager;
import sdk.chat.ui.fragments.AbstractChatFragment;
import sdk.chat.ui.fragments.ChatFragment;
import sdk.chat.ui.fragments.ContactsFragment;
import sdk.chat.ui.fragments.PrivateThreadsFragment;
import sdk.chat.ui.fragments.PublicThreadsFragment;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.provider.UIProvider;
import sdk.chat.ui.recycler.SmartViewModel;
import sdk.chat.ui.settings.SettingsActivity;
import sdk.chat.ui.utils.FragmentLifecycleManager;

public class ChatSDKUI {

    public interface ChatFragmentProvider {
        AbstractChatFragment provide(ThreadX thread);
    }

    protected static final ChatSDKUI instance = new ChatSDKUI();

    public ChatSDKUI() {
        ChatSDK.hook().addHook(Hook.sync(data -> {
            provider().holderProvider().clear();
        }), HookEvent.DidLogout);
    }

    protected MessageRegistrationManager messageRegistrationManager = new MessageRegistrationManager();
    protected FragmentLifecycleManager fragmentLifecycleManager = new FragmentLifecycleManager();

    protected List<SmartViewModel> settingsItems = new ArrayList<>();
    protected ProfileOption settingsProfileOption;

    protected ChatFragmentProvider chatFragmentProvider = (thread) -> {
        return new ChatFragment(thread);
    };

    public static ChatSDKUI shared() {
        return instance;
    }

    protected UIProvider provider = new UIProvider();

    public static UIProvider provider() {
        return shared().provider;
    }

    public static Icons icons() {
        return provider().icons();
    }

    public void setProvider(UIProvider provider) {
        this.provider = provider;
    }

    public UIProvider getProvider() {
        return provider;
    }

    public static void setChatFragmentProvider(ChatFragmentProvider provider) {
        shared().chatFragmentProvider = provider;
    }

    public static AbstractChatFragment getChatFragment(ThreadX thread) {
        return shared().chatFragmentProvider.provide(thread);
    }

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

    public static void setSettingsActivity(Class<? extends SettingsActivity> settingsActivity) {
        ChatSDK.ui().setSettingsActivity(settingsActivity);
    }

    public MessageRegistrationManager getMessageRegistrationManager() {
        return messageRegistrationManager;
    }

    public FragmentLifecycleManager getFragmentLifecycleManager() {
        return fragmentLifecycleManager;
    }

    public void stop() {
        messageRegistrationManager.stop();
    }

    /**
     * This populates the settings view. The smart view model items are automatically rendered
     * by the recycler view. For details, check out ViewHolders.kt
     * @param item
     */
    public void addSettingsItem(SmartViewModel item) {
        settingsItems.add(item);
        if (settingsProfileOption == null) {
            settingsProfileOption = new ProfileOption(ChatSDK.getString(R.string.settings), (activity, userEntityID) -> {
                Intent intent = new Intent(activity, ChatSDK.ui().getSettingsActivity());
                activity.startActivity(intent);
            }, User::isMe);
            ChatSDK.ui().addProfileOption(settingsProfileOption);
        }
    }

    public void removeSettingsItem(SmartViewModel item) {
        settingsItems.remove(item);
        if (settingsItems.isEmpty()) {
            ChatSDK.ui().removeProfileOption(settingsProfileOption);
        }
    }

    public List<SmartViewModel> getSettingsItems() {
        return settingsItems;
    }
}
