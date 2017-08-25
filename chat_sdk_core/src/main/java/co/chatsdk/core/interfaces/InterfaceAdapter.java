package co.chatsdk.core.interfaces;

import android.app.Activity;
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
    Fragment profileFragment ();

    List<Tab> defaultTabs ();

    Tab privateThreadsTab ();
    Tab publicThreadsTab ();
    Tab contactsTab ();
    Tab profileTab ();

    Activity profileActivity (User user);

}
