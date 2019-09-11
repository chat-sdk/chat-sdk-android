package co.chatsdk.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.interfaces.LocalNotificationHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.core.ui.ProfileFragmentProvider;
import co.chatsdk.core.utils.NotificationDisplayHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.handlers.ImageMessageDisplayHandler;
import co.chatsdk.ui.chat.handlers.LocationMessageDisplayHandler;
import co.chatsdk.ui.chat.handlers.TextMessageDisplayHandler;
import co.chatsdk.ui.chat.options.DialogChatOptionsHandler;
import co.chatsdk.ui.chat.options.LocationChatOption;
import co.chatsdk.ui.chat.options.MediaChatOption;
import co.chatsdk.ui.chat.options.MediaType;
import co.chatsdk.ui.threads.AddUsersToThreadActivity;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.threads.CreateThreadActivity;
import co.chatsdk.ui.contacts.ForwardMessageActivity;
import co.chatsdk.ui.login.LoginActivity;
import co.chatsdk.ui.login.SplashScreenActivity;
import co.chatsdk.ui.main.MainAppBarActivity;
import co.chatsdk.ui.profile.EditProfileActivity;
import co.chatsdk.ui.profile.ProfileActivity;
import co.chatsdk.ui.profile.ProfileFragment;
import co.chatsdk.ui.search.SearchActivity;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.threads.ThreadEditDetailsActivity;
import co.chatsdk.ui.threads.PublicThreadsFragment;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    private WeakReference<Context> context;

    public List<SearchActivityType> searchActivities = new ArrayList<>();
    public List<ChatOption> chatOptions = new ArrayList<>();
    public ChatOptionsHandler chatOptionsHandler = null;
    public Map<Integer,MessageDisplayHandler> messageHandlers = new HashMap<>();
    public boolean defaultChatOptionsAdded = false;
    public LocalNotificationHandler localNotificationHandler;
    public NotificationDisplayHandler notificationDisplayHandler;

    protected Class loginActivity = LoginActivity.class;
    protected Class splashScreenActivity = SplashScreenActivity.class;
    protected Class mainActivity = MainAppBarActivity.class;
    protected Class chatActivity = ChatActivity.class;
    protected Class threadDetailsActivity = ThreadDetailsActivity.class;
    protected Class threadEditDetailsActivity = ThreadEditDetailsActivity.class;

    protected Class searchActivity = SearchActivity.class;
    protected Class editProfileActivity = EditProfileActivity.class;
    protected Class profileActivity = ProfileActivity.class;
    protected Class createThreadActivity = CreateThreadActivity.class;
    protected Class addUsersToThreadActivity = AddUsersToThreadActivity.class;
    protected Class forwardMessageActivity = ForwardMessageActivity.class;

    protected Intent loginIntent;

    protected Fragment privateThreadsFragment = new PrivateThreadsFragment();
    protected Fragment publicThreadsFragment = new PublicThreadsFragment();
    protected Fragment contactsFragment = new ContactsFragment();
    protected ProfileFragmentProvider profileFragmentProvider = ProfileFragment::newInstance;

    private ArrayList<Tab> tabs = new ArrayList<>();
    private Tab privateThreadsTab;
    private Tab publicThreadsTab;
    private Tab contactsTab;
    private Tab profileTab;

    private String stringLocation;
    private String stringTakePhoto;
    private String stringChoosePhoto;

    public BaseInterfaceAdapter (Context context) {
        this.context = new WeakReference<>(context);

        DiskCacheConfig diskCacheConfig = DiskCacheConfig
                .newBuilder(context)
                .setMaxCacheSizeOnVeryLowDiskSpace(10 * ByteConstants.MB)
                .setMaxCacheSizeOnLowDiskSpace(20 * ByteConstants.MB)
                .setMaxCacheSize(40 * ByteConstants.MB)
                .build();

        Set<RequestListener> requestListeners = new HashSet<>();

//        requestListeners.add(new RequestLoggingListener());

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                // other setters
                .setRequestListeners(requestListeners)
                .setMainDiskCacheConfig(diskCacheConfig)
                .build();
        Fresco.initialize(context, config);
//        FLog.setMinimumLoggingLevel(FLog.VERBOSE);

        setMessageHandler(new TextMessageDisplayHandler(), new MessageType(MessageType.Text));
        setMessageHandler(new ImageMessageDisplayHandler(), new MessageType(MessageType.Image));
        setMessageHandler(new LocationMessageDisplayHandler(), new MessageType(MessageType.Location));

        stringLocation = context.getResources().getString(R.string.location);
        stringTakePhoto = context.getResources().getString(R.string.take_photo);
        stringChoosePhoto = context.getResources().getString(R.string.choose_photo);

    }

    @Override
    public List<Tab> defaultTabs() {
        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(privateThreadsTab());
        tabs.add(publicThreadsTab());
        tabs.add(contactsTab());
        tabs.add(profileTab());
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
    public void addTab(String title, int icon, Fragment fragment) {
        addTab(new Tab(title, icon, fragment));
    }

    @Override
    public void addTab(String title, int icon, Fragment fragment, int index) {
        addTab(new Tab(title, icon, fragment), index);
    }

    @Override
    public void removeTab(int index) {
        tabs().remove(index);
    }

    @Override
    public Tab privateThreadsTab() {
        if (privateThreadsTab == null) {
            privateThreadsTab = new Tab(context.get().getString(R.string.conversations), R.drawable.ic_action_private, privateThreadsFragment());
        }
        return privateThreadsTab;
    }

    @Override
    public Tab publicThreadsTab() {
        if (publicThreadsTab == null) {
            publicThreadsTab = new Tab(context.get().getString(R.string.chat_rooms), R.drawable.ic_action_public, publicThreadsFragment());
        }
        return publicThreadsTab;
    }

    @Override
    public Tab contactsTab() {
        if (contactsTab == null) {
            contactsTab = new Tab(context.get().getString(R.string.contacts), R.drawable.ic_action_contacts, contactsFragment());
        }
        return contactsTab;
    }

    @Override
    public Tab profileTab() {
        if (profileTab == null) {
            profileTab = new Tab (context.get().getString(R.string.profile), R.drawable.ic_action_user, profileFragment(null));
        }
        return profileTab;
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

    public Intent intentForActivity(Context context, Class activity, HashMap<String, Object> extras) {
        Intent intent = new Intent(context, activity);
        addExtrasToIntent(intent, extras);
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

    public void startActivity(Context context, Class activity, HashMap<String, Object> extras){
        startActivity(context, intentForActivity(context, activity, extras));
    }

    public void startActivity(Context context, Class activity){
        startActivity(context, activity, null);
    }

    public void startActivity (Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startActivityForResult (Activity activity, Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }

    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getChatActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        startActivity(context, intent);
    }

    /**
     * @deprecated use {@link #getLoginIntent(Context, HashMap)} ()}
     */
    @Deprecated
//    public void startLoginActivity(Context context, boolean attemptCachedLogin){
//        Intent intent = new Intent(context, getSplashScreenActivity());
//        startActivity(context, intent);
//    }
//
//    public void startLoginActivity (Context context, HashMap<String, Object> extras) {
//        startActivity(context, getLoginActivity(), extras);
//    }

    @Override
    public Intent getLoginIntent(Context context, HashMap<String, Object> extras) {
        if (loginIntent != null) {
            addExtrasToIntent(loginIntent, extras);
            return loginIntent;
        }
        return intentForActivity(context, getLoginActivity(), extras);
    }

    @Override
    public void setLoginIntent (Intent intent) {
        loginIntent = intent;
    }

    public void startSplashScreenActivity (Context context) {
        startActivity(context, getSplashScreenActivity());
    }

    public void startEditProfileActivity(Context context, String userEntityID){
        Intent intent = new Intent(context, getEditProfileActivity());
        intent.putExtra(Keys.IntentKeyUserEntityID, userEntityID);
        startActivity(context, intent);
    }

    public void startPublicThreadEditDetailsActivity(Context context, String threadEntityID){
        startThreadEditDetailsActivity(context, threadEntityID);
    }

    public void startThreadEditDetailsActivity(Context context, String threadEntityID){
        startThreadEditDetailsActivity(context, threadEntityID, null);
    }

    public void startThreadEditDetailsActivity(Context context, String threadEntityID, ArrayList<String> userEntityIDs){
        Intent intent = new Intent(context, getThreadEditDetailsActivity());
        if (threadEntityID != null) {
            intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        }
        if (userEntityIDs != null) {
            intent.putStringArrayListExtra(Keys.IntentKeyUserEntityIDList, userEntityIDs);
        }
        startActivity(context, intent);
    }

    public void startMainActivity (Context context, HashMap<String, Object> extras) {
        startActivity(context, getMainActivity(), extras);
    }

    public void startMainActivity (Context context) {
        startMainActivity(context, null);
    }

    public void startSearchActivity (Context context) {
        startActivity(context, getSearchActivity());
    }

    @Override
    public void startForwardMessageActivityForResult(Activity activity, Message message, int code) {
        Intent intent = new Intent(activity, getForwardMessageActivity());
        intent.putExtra(Keys.IntentKeyMessageEntityID, message.getEntityID());
        startActivityForResult(activity, intent, code);
    }

    @Override
    public void startAddUsersToThreadActivity(Context context) {
        startActivity(context, getAddUsersToThreadActivity());
    }

    @Override
    public void startCreateThreadActivity(Context context) {
        startActivity(context, getCreateThreadActivity());
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
                chatOptions.add(new MediaChatOption(stringTakePhoto, MediaType.takePhoto()));
                chatOptions.add(new MediaChatOption(stringChoosePhoto, MediaType.choosePhoto()));
            }
            defaultChatOptionsAdded = true;
        }

        return chatOptions;
    }

    public void startProfileActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getProfileActivity());
        intent.putExtra(Keys.IntentKeyUserEntityID, userEntityID);
        startActivity(context, intent);
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
    public void setMessageHandler(MessageDisplayHandler handler, MessageType type) {
        messageHandlers.put(type.value(), handler);
    }

    @Override
    public void removeMessageHandler(MessageType type) {
        MessageDisplayHandler handler = getMessageHandler(type);
        if (handler != null) {
            messageHandlers.remove(handler);
        }
    }

    @Override
    public Collection<MessageDisplayHandler> getMessageHandlers() {
        return messageHandlers.values();
    }

    @Override
    public MessageDisplayHandler getMessageHandler(MessageType type) {
        return messageHandlers.get(type.value());
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

}
