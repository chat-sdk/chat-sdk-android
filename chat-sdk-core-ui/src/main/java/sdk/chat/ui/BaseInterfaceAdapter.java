package sdk.chat.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sdk.chat.core.Tab;
import sdk.chat.core.avatar.AvatarGenerator;
import sdk.chat.core.avatar.HashAvatarGenerator;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ChatOption;
import sdk.chat.core.interfaces.ChatOptionsDelegate;
import sdk.chat.core.interfaces.ChatOptionsHandler;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.interfaces.LocalNotificationHandler;
import sdk.chat.core.notifications.NotificationDisplayHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.SearchActivityType;
import sdk.chat.core.ui.ProfileFragmentProvider;
import sdk.chat.core.utils.ProfileOption;
import sdk.chat.ui.activities.AddUsersToThreadActivity;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.activities.CreateThreadActivity;
import sdk.chat.ui.activities.EditProfileActivity;
import sdk.chat.ui.activities.EditThreadActivity;
import sdk.chat.ui.activities.ForwardMessageActivity;
import sdk.chat.ui.activities.LoginActivity;
import sdk.chat.ui.activities.MainAppBarActivity;
import sdk.chat.ui.activities.PostRegistrationActivity;
import sdk.chat.ui.activities.ProfileActivity;
import sdk.chat.ui.activities.SearchActivity;
import sdk.chat.ui.activities.SplashScreenActivity;
import sdk.chat.ui.activities.ThreadDetailsActivity;
import sdk.chat.ui.chat.options.DialogChatOptionsHandler;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;
import sdk.chat.ui.custom.BaseProfileFragmentProvider;
import sdk.chat.ui.custom.MessageCustomizer;
import sdk.chat.ui.fragments.ContactsFragment;
import sdk.chat.ui.fragments.PrivateThreadsFragment;
import sdk.chat.ui.fragments.PublicThreadsFragment;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    private WeakReference<Context> context;

    public List<SearchActivityType> searchActivities = new ArrayList<>();
    public List<ChatOption> chatOptions = new ArrayList<>();
    public ChatOptionsHandler chatOptionsHandler = null;
    public boolean defaultChatOptionsAdded = false;
    public LocalNotificationHandler localNotificationHandler;
    public NotificationDisplayHandler notificationDisplayHandler;

    protected Class<? extends Activity> loginActivity = LoginActivity.class;
    protected Class<? extends Activity> splashScreenActivity = SplashScreenActivity.class;
    protected Class<? extends Activity> mainActivity = MainAppBarActivity.class;
    protected Class<? extends Activity> chatActivity = ChatActivity.class;
    protected Class<? extends Activity> threadDetailsActivity = ThreadDetailsActivity.class;
    protected Class<? extends Activity> editThreadActivity = EditThreadActivity.class;
    protected Class<? extends Activity> postRegistrationActivity = PostRegistrationActivity.class;

    protected Class<? extends Activity> searchActivity = SearchActivity.class;
    protected Class<? extends Activity> editProfileActivity = EditProfileActivity.class;
    protected Class<? extends Activity> profileActivity = ProfileActivity.class;
    protected Class<? extends Activity> createThreadActivity = CreateThreadActivity.class;
    protected Class<? extends Activity> addUsersToThreadActivity = AddUsersToThreadActivity.class;
    protected Class<? extends Activity> forwardMessageActivity = ForwardMessageActivity.class;
    protected AvatarGenerator avatarGenerator = new HashAvatarGenerator();

    protected Intent loginIntent;

    protected Fragment privateThreadsFragment = new PrivateThreadsFragment();
    protected Fragment publicThreadsFragment = new PublicThreadsFragment();
    protected Fragment contactsFragment = new ContactsFragment();
    protected ProfileFragmentProvider profileFragmentProvider = new BaseProfileFragmentProvider();

//    private ArrayList<Tab> tabs = new ArrayList<>();
    protected Map<Integer, Tab> extraTabs = new HashMap<>();
    protected List<Integer> removeTabs = new ArrayList<>();

    protected Tab privateThreadsTab;
    protected Tab publicThreadsTab;
    protected Tab contactsTab;

    protected List<ProfileOption> profileOptions = new ArrayList<>();

    public BaseInterfaceAdapter (Context context) {
        this.context = new WeakReference<>(context);

        Icons.shared().initialize(context);

        searchActivities.add(new SearchActivityType(searchActivity, context.getString(R.string.search_with_name)));

    }

    @Override
    public List<Tab> defaultTabs() {
        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(privateThreadsTab());
        if (UIModule.config().publicRoomRoomsEnabled) {
            tabs.add(publicThreadsTab());
        }
        tabs.add(contactsTab());
        return tabs;
    }

    @Override
    public List<Tab> tabs() {
        List<Tab> tabs = new ArrayList<>();
        tabs.addAll(defaultTabs());

        for (int i: removeTabs) {
            if (i >= 0 && i < tabs.size()) {
                tabs.remove(i);
            }
        }

        List<Integer> indexes = new ArrayList<>(extraTabs.keySet());
        Collections.sort(indexes, Integer::compareTo);

        for (Integer i: indexes) {
            tabs.add(i < tabs.size() ? i : tabs.size(), extraTabs.get(i));
        }

        return tabs;
    }

    @Override
    public void setTab(Tab tab, int index) {
        extraTabs.put(index, tab);
    }

    @Override
    public void setTab(String title, Drawable icon, Fragment fragment, int index) {
        setTab(new Tab(title, icon, fragment), index);
    }

    @Override
    public void removeTab(int index) {
        removeTabs.add(index);
    }

    @Override
    public Tab privateThreadsTab() {
        if (privateThreadsTab == null) {
            privateThreadsTab = new Tab(String.format(context.get().getString(R.string.conversations__), ""), Icons.get(context.get(), Icons.choose().chat, Icons.shared().tabIconColor), privateThreadsFragment());
        }
        return privateThreadsTab;
    }

    @Override
    public Tab publicThreadsTab() {
        if (publicThreadsTab == null) {
            publicThreadsTab = new Tab(context.get().getString(R.string.chat_rooms), Icons.get(context.get(), Icons.choose().publicChat, Icons.shared().tabIconColor), publicThreadsFragment());
        }
        return publicThreadsTab;
    }

    @Override
    public Tab contactsTab() {
        if (contactsTab == null) {
            contactsTab = new Tab(context.get().getString(R.string.contacts), Icons.get(context.get(), Icons.choose().contact, Icons.shared().tabIconColor), contactsFragment());
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
    public Class<? extends Activity> getLoginActivity() {
        return loginActivity;
    }

    @Override
    public void setLoginActivity (Class<? extends Activity> loginActivity) {
        this.loginActivity = loginActivity;
    }

    @Override
    public Class<? extends Activity> getSplashScreenActivity() {
        return splashScreenActivity;
    }

    @Override
    public void setSplashScreenActivity (Class<? extends Activity> splashScreenActivity) {
        this.splashScreenActivity = splashScreenActivity;
    }

    @Override
    public Class<? extends Activity> getMainActivity() {
        return mainActivity;
    }

    @Override
    public Class<? extends Activity> getPostRegistrationActivity() {
        return postRegistrationActivity;
    }

    @Override
    public void setMainActivity (Class<? extends Activity> mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public Class<? extends Activity> getChatActivity() {
        return chatActivity;
    }

    @Override
    public void setChatActivity (Class<? extends Activity> chatActivity) {
        this.chatActivity = chatActivity;
    }

    @Override
    public Class<? extends Activity> getThreadDetailsActivity() {
        return ThreadDetailsActivity.class;
    }

    @Override
    public void setThreadDetailsActivity (Class<? extends Activity> threadDetailsActivity) {
        this.threadDetailsActivity = threadDetailsActivity;
    }

    @Override
    public Class<? extends Activity> getThreadEditDetailsActivity() {
        return getEditThreadActivity();
    }

    @Override
    public Class<? extends Activity> getEditThreadActivity() {
        return editThreadActivity;
    }

    @Override
    public void setThreadEditDetailsActivity (Class<? extends Activity> threadEditDetailsActivity) {
        setEditThreadActivity(threadEditDetailsActivity);
    }

    @Override
    public void setEditThreadActivity(Class<? extends Activity> editThreadActivity) {
        this.editThreadActivity = editThreadActivity;
    }

    @Override
    public void setPostRegistrationActivity (Class<? extends Activity> postRegistrationActivity) {
        this.postRegistrationActivity = postRegistrationActivity;
    }

    @Override
    public void setForwardMessageActivity(Class<? extends Activity> forwardMessageActivity) {
        this.forwardMessageActivity = forwardMessageActivity;
    }

    @Override
    public Class<? extends Activity> getAddUsersToThreadActivity() {
        return addUsersToThreadActivity;
    }

    @Override
    public Class<? extends Activity> getCreateThreadActivity() {
        return createThreadActivity;
    }

    @Override
    public Class<? extends Activity> getForwardMessageActivity() {
        return forwardMessageActivity;
    }

    @Override
    public void setAddUsersToThreadActivity(Class<? extends Activity> addUsersToThreadActivity) {
        this.addUsersToThreadActivity = addUsersToThreadActivity;
    }

    @Override
    public void setCreateThreadActivity(Class<? extends Activity> createThreadActivity) {
        this.createThreadActivity = createThreadActivity;
    }

    @Override
    public Class<? extends Activity> getSearchActivity() {
        return searchActivity;
    }


    @Override
    public void setSearchActivity (Class<? extends Activity> searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
    public Class<? extends Activity> getEditProfileActivity() {
        return editProfileActivity;
    }

    @Override
    public void setEditProfileActivity (Class<? extends Activity> editProfileActivity) {
        this.editProfileActivity = editProfileActivity;
    }

    @Override
    public Class<? extends Activity> getProfileActivity() {
        return profileActivity;
    }

    @Override
    public void setProfileActivity (Class<? extends Activity> profileActivity) {
        this.profileActivity = profileActivity;
    }

    public Intent intentForActivity(Context context, Class<? extends Activity> activity, HashMap<String, Object> extras, int flags) {
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

    public void startActivity(Context context, Class<? extends Activity> activity, HashMap<String, Object> extras, int flags) {
        startActivity(context, intentForActivity(context, activity, extras, flags));
    }

    public void startActivity(Context context, Class<? extends Activity> activity){
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
        startChatActivityForID(context, threadEntityID, null);
    }

    public void startChatActivityForID(Context context, String threadEntityID, @Nullable Integer flags) {
        Intent intent = new Intent(context, getChatActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        if (flags != null) {
            intent.setFlags(flags);
        }
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
    public void addSearchActivity(Class<? extends Activity> className, String title, int requestCode) {
        SearchActivityType activity = new SearchActivityType(className, title, requestCode);
        removeSearchActivity(className);
        searchActivities.add(activity);
    }

    @Override
    public void addSearchActivity(Class<? extends Activity> className, String title) {
        addSearchActivity(className, title, -1);
    }

    @Override
    public void removeSearchActivity(Class<? extends Activity> className) {
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

            if(UIModule.config().imageMessagesEnabled) {
                //chatOptions.add(new MediaChatOption(stringTakePhoto, MediaType.takePhoto()));
                chatOptions.add(new MediaChatOption(context.get().getResources().getString(R.string.image_or_photo), MediaType.choosePhoto()));
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

    public void startPostRegistrationActivity(Context context, HashMap<String, Object> extras) {
        startActivity(context, postRegistrationActivity, extras, 0);
    }

    public void addProfileOption(ProfileOption option) {
        profileOptions.add(option);
    }

    public void removeProfileOption(ProfileOption option) {
        profileOptions.remove(option);
    }

    public List<ProfileOption> getProfileOptions() {
        return profileOptions;
    }

    public void stop() {
        MessageCustomizer.shared().stop();
        searchActivities.clear();
        chatOptions.clear();
        extraTabs.clear();
        removeTabs.clear();
        profileOptions.clear();
    }

}
