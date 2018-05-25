package co.chatsdk.ui.manager;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.core.interfaces.LocalNotificationHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.core.utils.NotificationDisplayHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.handlers.ImageMessageDisplayHandler;
import co.chatsdk.ui.chat.handlers.LocationMessageDisplayHandler;
import co.chatsdk.ui.chat.handlers.TextMessageDisplayHandler;
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
import co.chatsdk.ui.search.SearchActivity;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.threads.ThreadEditDetailsActivity;
import co.chatsdk.ui.threads.PublicThreadsFragment;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

public class BaseInterfaceAdapter implements InterfaceAdapter {

    public List<SearchActivityType> searchActivities = new ArrayList<>();
    public List<ChatOption> chatOptions = new ArrayList<>();
    public ChatOptionsHandler chatOptionsHandler = null;
    public Map<Integer,MessageDisplayHandler> messageHandlers = new HashMap<>();
    public boolean defaultChatOptionsAdded = false;
    public LocalNotificationHandler localNotificationHandler;
    public NotificationDisplayHandler notificationDisplayHandler;

    public BaseInterfaceAdapter (Context context) {

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
        return new Tab (R.string.profile, R.drawable.ic_action_user, profileFragment(null));
    }

    @Override
    public AppCompatActivity profileActivity(User user) {
        return null;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return new PrivateThreadsFragment();
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
    public Class getThreadEditDetailsActivity() {
        return ThreadEditDetailsActivity.class;
    }

    @Override
    public Class getSelectContactActivity() {
        return SelectContactActivity.class;
    }

    @Override
    public Class getSearchActivity() {
        return SearchActivity.class;
    }

    @Override
    public Class getEditProfileActivity() {
        return EditProfileActivity.class;
    }

    @Override
    public Class getProfileActivity() {
        return ProfileActivity.class;
    }

    public void startActivity(Context context, Class activity, HashMap<String, Object> extras){
        Intent intent = new Intent(context, activity);
        if (extras != null) {
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
        startActivity(context, intent);
    }

    public void startActivity(Context context, Class activity){
        startActivity(context, activity, null);
    }

    public void startActivity (Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getChatActivity());
        intent.putExtra(InterfaceManager.THREAD_ENTITY_ID, threadEntityID);
        startActivity(context, intent);
    }

    public void startLoginActivity(Context context, boolean attemptCachedLogin){
        Intent intent = new Intent(context, getLoginActivity());
        intent.putExtra(InterfaceManager.ATTEMPT_CACHED_LOGIN, attemptCachedLogin);
        startActivity(context, intent);
    }

    public void startEditProfileActivity(Context context, String userEntityID){
        Intent intent = new Intent(context, getEditProfileActivity());
        intent.putExtra(InterfaceManager.USER_ENTITY_ID, userEntityID);
        startActivity(context, intent);
    }

    public void startPublicThreadEditDetailsActivity(Context context, String threadEntityID){
        Intent intent = new Intent(context, getThreadEditDetailsActivity());
        intent.putExtra(InterfaceManager.THREAD_ENTITY_ID, threadEntityID);
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
        // Setup the default chat options
        if (!defaultChatOptionsAdded) {
            if(ChatSDK.config().locationMessagesEnabled) {
                chatOptions.add(new LocationChatOption("Location"));
            }

            if(ChatSDK.config().imageMessagesEnabled) {
                chatOptions.add(new MediaChatOption("Take Photo", MediaChatOption.Type.TakePhoto));
                chatOptions.add(new MediaChatOption("Choose Photo", MediaChatOption.Type.ChoosePhoto));
            }
            defaultChatOptionsAdded = true;
        }

        return chatOptions;
    }

    public void startProfileActivity(Context context, String userEntityID) {
        Intent intent = new Intent(context, getProfileActivity());
        intent.putExtra(InterfaceManager.USER_ENTITY_ID, userEntityID);
        startActivity(context, intent);
    }

    @Override
    public void setChatOptionsHandler(ChatOptionsHandler handler) {
        chatOptionsHandler = handler;
    }

    @Override
    public ChatOptionsHandler getChatOptionsHandler(ChatOptionsDelegate delegate) {
        chatOptionsHandler = new DialogChatOptionsHandler(delegate);
        chatOptionsHandler.setDelegate(delegate);
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
