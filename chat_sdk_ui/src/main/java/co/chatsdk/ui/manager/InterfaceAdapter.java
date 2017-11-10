package co.chatsdk.ui.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.interfaces.ChatOptionsHandler;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.types.SearchActivityType;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public interface InterfaceAdapter {

    Fragment privateThreadsFragment ();
    Fragment publicThreadsFragment ();
    Fragment contactsFragment ();
    Fragment profileFragment (User user);

    Class getLoginActivity();
    Class getMainActivity();
    Class getChatActivity();
    Class getThreadDetailsActivity();
    Class getSelectContactActivity();
    Class getSearchActivity();
    Class getEditProfileActivity();
    Class getProfileActivity();

    List<Tab> defaultTabs ();

    Tab privateThreadsTab ();
    Tab publicThreadsTab ();
    Tab contactsTab ();
    Tab profileTab ();

    Activity profileActivity (User user);

    void startActivity(Context context, Class activity);
    void startActivity (Context context, Intent intent);
    void startChatActivityForID(Context context, String threadEntityID);
    void startLoginActivity(Context context, boolean attemptCachedLogin);
    void startProfileActivity(Context context, String userEntityID);
    void startEditProfileActivity(Context context, String userEntityID);
    void startMainActivity (Context context);
    void startSearchActivity (Context context);
    void startSelectContactsActivity(Context context);

    void addSearchActivity (Class className, String name);
    void removeSearchActivity (Class className);
    List<SearchActivityType> getSearchActivities ();

    void addChatOption (ChatOption option);
    void removeChatOption (ChatOption option);
    List<ChatOption> getChatOptions();

    void setChatOptionsHandler (ChatOptionsHandler handler);
    ChatOptionsHandler getChatOptionsHandler (ChatOptionsDelegate delegate);

    void addCustomMessageHandler (CustomMessageHandler handler);
    void removeCustomMessageHandler (CustomMessageHandler handler);
    List<CustomMessageHandler> getCustomMessageHandlers();
}
