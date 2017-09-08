package co.chatsdk.core.interfaces;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;

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

    void startActivity(Class activity);
    void startActivity (Intent intent);
    void startChatActivityForID(String threadEntityID);
    void startLoginActivity(boolean attemptCachedLogin);
    void startProfileActivity(String userEntityID);
    void startEditProfileActivity(String userEntityID);
    void startMainActivity ();
    void startSearchActivity ();
    void startSelectContactsActivity();
}
