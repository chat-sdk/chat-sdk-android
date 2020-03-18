package co.chatsdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.avatar.FlatHashAvatarGenerator;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.interfaces.LocalNotificationHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.core.ui.ProfileFragmentProvider;
import co.chatsdk.core.notifications.NotificationDisplayHandler;
import co.chatsdk.core.avatar.AvatarGenerator;
import co.chatsdk.ui.chat.options.DialogChatOptionsHandler;
import co.chatsdk.ui.chat.options.LocationChatOption;
import co.chatsdk.ui.chat.options.MediaChatOption;
import co.chatsdk.ui.chat.options.MediaType;
import co.chatsdk.ui.activities.ChatActivity;
import co.chatsdk.ui.custom.BaseProfileFragmentProvider;
import co.chatsdk.ui.fragments.PrivateThreadsFragment;
import co.chatsdk.ui.fragments.PublicThreadsFragment;
import co.chatsdk.ui.activities.AddUsersToThreadActivity;
import co.chatsdk.ui.fragments.ContactsFragment;
import co.chatsdk.ui.activities.CreateThreadActivity;
import co.chatsdk.ui.activities.ForwardMessageActivity;
import co.chatsdk.ui.activities.LoginActivity;
import co.chatsdk.ui.activities.SplashScreenActivity;
import co.chatsdk.ui.activities.MainAppBarActivity;
import co.chatsdk.ui.activities.EditProfileActivity;
import co.chatsdk.ui.activities.ProfileActivity;
import co.chatsdk.ui.fragments.ProfileFragment;
import co.chatsdk.ui.activities.SearchActivity;
import co.chatsdk.ui.activities.EditThreadActivity;
import co.chatsdk.ui.activities.ThreadDetailsActivity;
import co.chatsdk.ui.icons.Icons;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    private WeakReference<Context> context;

    public List<SearchActivityType> searchActivities = new ArrayList<>();
    public List<ChatOption> chatOptions = new ArrayList<>();
    public ChatOptionsHandler chatOptionsHandler = null;
    public boolean defaultChatOptionsAdded = false;
    public LocalNotificationHandler localNotificationHandler;
    public NotificationDisplayHandler notificationDisplayHandler;

    protected Class loginActivity = LoginActivity.class;
    protected Class splashScreenActivity = SplashScreenActivity.class;
    protected Class mainActivity = MainAppBarActivity.class;
    protected Class chatActivity = ChatActivity.class;
    protected Class threadDetailsActivity = ThreadDetailsActivity.class;
    protected Class threadEditDetailsActivity = EditThreadActivity.class;

    protected Class searchActivity = SearchActivity.class;
    protected Class editProfileActivity = EditProfileActivity.class;
    protected Class profileActivity = ProfileActivity.class;
    protected Class createThreadActivity = CreateThreadActivity.class;
    protected Class addUsersToThreadActivity = AddUsersToThreadActivity.class;
    protected Class forwardMessageActivity = ForwardMessageActivity.class;
    protected AvatarGenerator avatarGenerator = new FlatHashAvatarGenerator();

    protected Intent loginIntent;

    protected Fragment privateThreadsFragment = new PrivateThreadsFragment();
    protected Fragment publicThreadsFragment = new PublicThreadsFragment();
    protected Fragment contactsFragment = new ContactsFragment();
    protected ProfileFragmentProvider profileFragmentProvider = new BaseProfileFragmentProvider();

    protected @DrawableRes int defaultProfileImage = R.drawable.icn_100_profile;

    private ArrayList<Tab> tabs = new ArrayList<>();
    private Tab privateThreadsTab;
    private Tab publicThreadsTab;
    private Tab contactsTab;

    private String stringLocation;
    private String stringChoosePhoto;

    public BaseInterfaceAdapter (Context context) {
        this.context = new WeakReference<>(context);

        Icons.shared().setContext(context);

        stringLocation = context.getResources().getString(R.string.location);
        stringChoosePhoto = context.getResources().getString(R.string.image_or_photo);

        if (ChatSDK.config().profileHeaderImage == 0) {
            ChatSDK.config().profileHeaderImage = R.drawable.header2;
        }
        if (ChatSDK.config().drawerHeaderImage == 0) {
            ChatSDK.config().drawerHeaderImage = R.drawable.header2;
        }
    }

    @Override
    public List<Tab> defaultTabs() {
        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(privateThreadsTab());
        tabs.add(publicThreadsTab());
        tabs.add(contactsTab());
        return tabs;
    }

    @Override
    public List<Tab> tabs() {
        if (tabs.size() == 0) {
            tabs.addAll(defaultTabs());
        }
        return tabs;
    }

    @Override
    public void addTab(Tab tab) {
        tabs().add(tab);
    }

    @Override
    public void addTab(Tab tab, int index) {
        tabs().add(index, tab);
    }

    @Override
    public void addTab(String title, Drawable icon, Fragment fragment) {
        addTab(new Tab(title, icon, fragment));
    }

    @Override
    public void addTab(String title, Drawable icon, Fragment fragment, int index) {
        addTab(new Tab(title, icon, fragment), index);
    }

    @Override
    public void removeTab(int index) {
        tabs().remove(index);
    }

    @Override
    public Tab privateThreadsTab() {
        if (privateThreadsTab == null) {
            privateThreadsTab = new Tab(String.format(context.get().getString(R.string.conversations__), ""), Icons.get(Icons.choose().chat, R.color.tab_icon_color), privateThreadsFragment());
        }
        return privateThreadsTab;
    }

    @Override
    public Tab publicThreadsTab() {
        if (publicThreadsTab == null) {
            publicThreadsTab = new Tab(context.get().getString(R.string.chat_rooms), Icons.get(Icons.choose().publicChat, R.color.tab_icon_color), publicThreadsFragment());
        }
        return publicThreadsTab;
    }

    @Override
    public Tab contactsTab() {
        if (contactsTab == null) {
            contactsTab = new Tab(context.get().getString(R.string.contacts), Icons.get(Icons.choose().contact, R.color.tab_icon_color), contactsFragment());
        }
        return contactsTab;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return privateThreadsFragment;
    }

    @Override
    public void setPrivateThreadsFragment (Fragment privateThreadsFragment) {
        this.privateThreadsFragment = privateThreadsFragment;
    }

    @Override
    public Fragment publicThreadsFragment() {
        return publicThreadsFragment;
    }

    @Override
    public void setPublicThreadsFragment (Fragment publicThreadsFragment) {
        this.publicThreadsFragment = publicThreadsFragment;
    }

    @Override
    public Fragment contactsFragment() {
        return contactsFragment;
    }

    @Override
    public void setContactsFragment (Fragment contactsFragment) {
        this.contactsFragment = contactsFragment;
    }

    @Override
    public Fragment profileFragment(User user) {
        return profileFragmentProvider.profileFragment(user);
    }

    @Override
    public void setProfileFragmentProvider (ProfileFragmentProvider profileFragmentProvider) {
        this.profileFragmentProvider = profileFragmentProvider;
    }

    @Override
    public Class getLoginActivity() {
        return loginActivity;
    }

    @Override
    public void setLoginActivity (Class loginActivity) {
        this.loginActivity = loginActivity;
    }

    @Override
    public Class getSplashScreenActivity() {
        return splashScreenActivity;
    }

    @Override
    public void setSplashScreenActivity (Class splashScreenActivity) {
        this.splashScreenActivity = splashScreenActivity;
    }

    @Override
    public Class getMainActivity() {
        return mainActivity;
    }

    @Override
    public void setMainActivity (Class mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public Class getChatActivity() {
        return chatActivity;
    }

    @Override
    public void setChatActivity (Class chatActivity) {
        this.chatActivity = chatActivity;
    }

    @Override
    public Class getThreadDetailsActivity() {
        return ThreadDetailsActivity.class;
    }

    @Override
    public void setThreadDetailsActivity (Class threadDetailsActivity) {
        this.threadDetailsActivity = threadDetailsActivity;
    }

    @Override
    public Class getThreadEditDetailsActivity() {
        return threadEditDetailsActivity;
    }

    @Override
    public void setThreadEditDetailsActivity (Class threadEditDetailsActivity) {
        this.threadEditDetailsActivity = threadEditDetailsActivity;
    }

    @Override
    public void setForwardMessageActivity(Class forwardMessageActivity) {
        this.forwardMessageActivity = forwardMessageActivity;
    }

    @Override
    public Class getAddUsersToThreadActivity() {
        return addUsersToThreadActivity;
    }

    @Override
    public Class getCreateThreadActivity() {
        return createThreadActivity;
    }

    @Override
    public Class getForwardMessageActivity() {
        return forwardMessageActivity;
    }

    @Override
    public void setAddUsersToThreadActivity(Class addUsersToThreadActivity) {
        this.addUsersToThreadActivity = addUsersToThreadActivity;
    }

    @Override
    public void setCreateThreadActivity(Class createThreadActivity) {
        this.createThreadActivity = createThreadActivity;
    }

    @Override
    public Class getSearchActivity() {
        return searchActivity;
    }


    @Override
    public void setSearchActivity (Class searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
    public Class getEditProfileActivity() {
        return editProfileActivity;
    }

    @Override
    public void setEditProfileActivity (Class editProfileActivity) {
        this.editProfileActivity = editProfileActivity;
    }

    @Override
    public Class getProfileActivity() {
        return profileActivity;
    }

    @Override
    public void setProfileActivity (Class profileActivity) {
        this.profileActivity = profileActivity;
    }

    public Intent intentForActivity(Context context, Class activity, HashMap<String, Object> extras, int flags) {
        Intent intent = new Intent(context, activity);
        addExtrasToIntent(intent, extras);
        if (flags != 0) {
            intent.addFlags(flags);
        }
        return intent;
    }

    public void addExtrasToIntent (Intent intent, HashMap<String, Object> extras) {
        if (extras != null && intent != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (value instanceof String) {
                    intent.putExtra(key, (String) extras.get(key));
                }
                if (value instanceof Integer) {
                    intent.putExtra(key, (Integer) extras.get(key));
                }
                if (value instanceof Double) {
                    intent.putExtra(key, (Double) extras.get(key));
                }
                if (value instanceof Float) {
                    intent.putExtra(key, (Float) extras.get(key));
                }
            }
        }
    }

    public void startActivity(Context context, Class activity, HashMap<String, Object> extras, int flags) {
        startActivity(context, intentForActivity(context, activity, extras, flags));
    }

    public void startActivity(Context context, Class activity){
        startActivity(context, activity, null, 0);
    }

    public void startActivity (Context context, Intent intent) {
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startActivityForResult (Activity activity, Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    @Override
    public Intent getLoginIntent(Context context, HashMap<String, Object> extras) {
        if (loginIntent != null) {
            addExtrasToIntent(loginIntent, extras);
            return loginIntent;
        }
        return intentForActivity(context, getLoginActivity(), extras, 0);
    }

    @Override
    public void setLoginIntent (Intent intent) {
        loginIntent = intent;
    }

    public void startSplashScreenActivity (Context context) {
        startActivity(context, getSplashScreenActivity());
    }

    public void startEditProfileActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getEditProfileActivity());
        intent.putExtra(Keys.IntentKeyUserEntityID, userEntityID);
        context.startActivity(intent);
    }

    public void startPublicThreadEditDetailsActivity(Context context, String threadEntityID){
        startEditThreadActivity(context, threadEntityID);
    }

    public void startEditThreadActivity(Context context, String threadEntityID){
        startEditThreadActivity(context, threadEntityID, null);
    }

    @Override
    public void startCreateThreadActivity(Context context) {
        Intent intent = new Intent(context, getCreateThreadActivity());
        context.startActivity(intent);
    }

    public void startEditThreadActivity(Context context, String threadEntityID, ArrayList<String> userEntityIDs){
        Intent intent = new Intent(context, getThreadEditDetailsActivity());
        if (threadEntityID != null) {
            intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        }
        if (userEntityIDs != null) {
            intent.putStringArrayListExtra(Keys.IntentKeyUserEntityIDList, userEntityIDs);
        }
        context.startActivity(intent);
    }

    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getChatActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        context.startActivity(intent, new Bundle());
    }


    public void startMainActivity (Context context, HashMap<String, Object> extras) {
        startActivity(context, getMainActivity(), extras, Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public void startMainActivity (Context context) {
        startMainActivity(context, null);
    }

    public void startSearchActivity (Context context) {
        startActivity(context, getSearchActivity());
    }

    @Override
    public void startForwardMessageActivityForResult(Activity activity, Thread thread, List<Message> messages, int code) {
        Intent intent = new Intent(activity, getForwardMessageActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, thread.getEntityID());

        ArrayList<String> messageEntityIDs = new ArrayList<>();

        for (Message message: messages) {
            messageEntityIDs.add(message.getEntityID());
        }

        intent.putStringArrayListExtra(Keys.IntentKeyMessageEntityIDs, messageEntityIDs);
        startActivityForResult(activity, intent, code);
    }

    @Override
    public void startAddUsersToThreadActivity(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getAddUsersToThreadActivity());
        if (threadEntityID != null) {
            intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        }
        startActivity(context, intent);
    }

    @Override
    public void startThreadDetailsActivity(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getThreadDetailsActivity());
        if (threadEntityID != null) {
            intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        context.startActivity(intent);
        startActivity(context, intent);
    }


    @Override
    public void addSearchActivity(Class className, String title) {
        SearchActivityType activity = new SearchActivityType(className, title);
        removeSearchActivity(className);
        searchActivities.add(activity);
    }

    @Override
    public void removeSearchActivity(Class className) {
        Iterator<SearchActivityType> iterator = searchActivities.iterator();
        while (iterator.hasNext()) {
            if(iterator.next().className.equals(className)) {
                searchActivities.remove(iterator.next());
            }
        }
    }

    @Override
    public List<SearchActivityType> getSearchActivities() {
        return searchActivities;
    }

    @Override
    public void addChatOption(ChatOption option) {
        if(!chatOptions.contains(option)) {
            chatOptions.add(option);
        }
    }

    @Override
    public void removeChatOption(ChatOption option) {
        if(chatOptions.contains(option)) {
            chatOptions.remove(option);
        }
    }

    @Override
    public List<ChatOption> getChatOptions() {
        // Setup the default chat options
        if (!defaultChatOptionsAdded) {
            if(ChatSDK.config().locationMessagesEnabled) {
                chatOptions.add(new LocationChatOption(stringLocation));
            }

            if(ChatSDK.config().imageMessagesEnabled) {
                //chatOptions.add(new MediaChatOption(stringTakePhoto, MediaType.takePhoto()));
                chatOptions.add(new MediaChatOption(stringChoosePhoto, MediaType.choosePhoto()));
            }
            defaultChatOptionsAdded = true;
        }

        return chatOptions;
    }

    @Override
    public void startProfileActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getProfileActivity());
        intent.putExtra(Keys.IntentKeyUserEntityID, userEntityID);
        context.startActivity(intent);
    }

    @Override
    public void setChatOptionsHandler(ChatOptionsHandler handler) {
        chatOptionsHandler = handler;
    }

    @Override
    public ChatOptionsHandler getChatOptionsHandler(ChatOptionsDelegate delegate) {
        if (chatOptionsHandler == null) {
//            chatOptionsHandler = new FloatingChatOptionsHandler(delegate);
            chatOptionsHandler = new DialogChatOptionsHandler(delegate);
        } else {
            chatOptionsHandler.setDelegate(delegate);
        }
        return chatOptionsHandler;
    }

    @Override
    public boolean showLocalNotifications(Thread thread) {
        if (localNotificationHandler != null) {
            return localNotificationHandler.showLocalNotification(thread);
        }
        else {
            return ChatSDK.config().showLocalNotifications;
        }
    }

    @Override
    public void setLocalNotificationHandler(LocalNotificationHandler handler) {
        this.localNotificationHandler = handler;
    }

    public NotificationDisplayHandler notificationDisplayHandler () {
        if(notificationDisplayHandler == null) {
            notificationDisplayHandler = new NotificationDisplayHandler();
        }
        return notificationDisplayHandler;
    }

    public AvatarGenerator getAvatarGenerator() {
        return avatarGenerator;
    }

    public void setAvatarGenerator(AvatarGenerator avatarGenerator) {
        this.avatarGenerator = avatarGenerator;
    }

    public void setDefaultProfileImage(@DrawableRes int drawable) {
        defaultProfileImage = drawable;
    }

    public @DrawableRes int getDefaultProfileImage() {
        return defaultProfileImage;
    }

}
