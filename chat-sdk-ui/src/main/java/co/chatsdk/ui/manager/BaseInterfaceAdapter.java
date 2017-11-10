package co.chatsdk.ui.manager;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.options.DialogChatOptionsHandler;
import co.chatsdk.ui.chat.options.LocationChatOption;
import co.chatsdk.ui.chat.options.MediaChatOption;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.contacts.SelectContactActivity;
import co.chatsdk.ui.login.LoginActivity;
import co.chatsdk.ui.main.MainActivity;
import co.chatsdk.ui.profile.EditProfileActivity;
import co.chatsdk.ui.profile.ProfileActivity;
import co.chatsdk.ui.profile.ProfileFragment;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.threads.PublicThreadsFragment;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    public static String USER_ENTITY_ID = "USER_ENTITY_ID";
    public static final String THREAD_ENTITY_ID = "THREAD_ENTITY_ID";
    public static final String ATTEMPT_CACHED_LOGIN = "ATTEMPT_CACHED_LOGIN";

    public List<SearchActivityType> searchActivities = new ArrayList<>();
    public List<ChatOption> chatOptions = new ArrayList<>();
    public ChatOptionsHandler chatOptionsHandler = null;
    public List<CustomMessageHandler> customMessageHandlers = new ArrayList<>();

    private WeakReference<Context> context;

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

        // Setup the default chat options
        if(ChatSDK.config().locationMessagesEnabled) {
            chatOptions.add(new LocationChatOption("Location"));
        }

        if(ChatSDK.config().imageMessagesEnabled) {
            chatOptions.add(new MediaChatOption("Take Photo", MediaChatOption.Type.TakePhoto));
            chatOptions.add(new MediaChatOption("Choose Photo", MediaChatOption.Type.ChoosePhoto));
        }

        this.context = new WeakReference<>(context);
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
    public Tab privateThreadsTab() {
        return new Tab(R.string.conversations, R.drawable.ic_action_private, privateThreadsFragment());
    }

    @Override
    public Tab publicThreadsTab() {
        return new Tab(R.string.chat_rooms, R.drawable.ic_action_public, publicThreadsFragment());
    }

    @Override
    public Tab contactsTab() {
        return new Tab(R.string.contacts, R.drawable.ic_action_contacts, contactsFragment());
    }

    @Override
    public Tab profileTab() {
        return new Tab (R.string.profile, R.drawable.ic_action_user, ProfileFragment.newInstance());
    }

    @Override
    public AppCompatActivity profileActivity(User user) {
        return null;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return PrivateThreadsFragment.newInstance();
    }

    @Override
    public Fragment publicThreadsFragment() {
        return new PublicThreadsFragment();
    }

    @Override
    public Fragment contactsFragment() {
        return ContactsFragment.newInstance();
    }

    @Override
    public Fragment profileFragment(User user) {
        return ProfileFragment.newInstance(user);
    }

    @Override
    public Class getLoginActivity() {
        return LoginActivity.class;
    }

    @Override
    public Class getMainActivity() {
        return MainActivity.class;
    }

    @Override
    public Class getChatActivity() {
        return ChatActivity.class;
    }

    @Override
    public Class getThreadDetailsActivity() {
        return ThreadDetailsActivity.class;
    }

    @Override
    public Class getSelectContactActivity() {
        return SelectContactActivity.class;
    }

    @Override
    public Class getSearchActivity() {
        return SearchActivityType.class;
    }

    @Override
    public Class getEditProfileActivity() {
        return EditProfileActivity.class;
    }

    @Override
    public Class getProfileActivity() {
        return ProfileActivity.class;
    }

    public void startActivity(Context context, Class activity){
        Intent intent = new Intent(context, activity);
        startActivity(context, intent);
    }

    public void startActivity (Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getChatActivity());
        intent.putExtra(THREAD_ENTITY_ID, threadEntityID);
        startActivity(context, intent);
    }

    public void startLoginActivity(Context context, boolean attemptCachedLogin){
        Intent intent = new Intent(context, getLoginActivity());
        intent.putExtra(ATTEMPT_CACHED_LOGIN, attemptCachedLogin);
        startActivity(context, intent);
    }

    public void startEditProfileActivity(Context context, String userEntityID){
        Intent intent = new Intent(context, getEditProfileActivity());
        intent.putExtra(USER_ENTITY_ID, userEntityID);
        startActivity(context, intent);
    }

    public void startMainActivity (Context context) {
        startActivity(context, getMainActivity());
    }

    public void startSearchActivity (Context context) {
        startActivity(context, getSearchActivity());
    }

    public void startSelectContactsActivity(Context context) {
        startActivity(context, getSelectContactActivity());
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
        return chatOptions;
    }

    public void startProfileActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getProfileActivity());
        intent.putExtra(USER_ENTITY_ID, userEntityID);
        startActivity(context, intent);
    }

    @Override
    public void setChatOptionsHandler(ChatOptionsHandler handler) {
        chatOptionsHandler = handler;
    }

    @Override
    public ChatOptionsHandler getChatOptionsHandler(ChatOptionsDelegate delegate) {
        if(chatOptionsHandler == null) {
            chatOptionsHandler = new DialogChatOptionsHandler(delegate);
        }
        chatOptionsHandler.setDelegate(delegate);
        return chatOptionsHandler;
    }

    @Override
    public void addCustomMessageHandler(CustomMessageHandler handler) {
        if(!customMessageHandlers.contains(handler)) {
            customMessageHandlers.add(handler);
        }
    }

    @Override
    public void removeCustomMessageHandler(CustomMessageHandler handler) {
        if(customMessageHandlers.contains(handler)) {
            customMessageHandlers.remove(handler);
        }
    }

    @Override
    public List<CustomMessageHandler> getCustomMessageHandlers() {
        return customMessageHandlers;
    }


}
