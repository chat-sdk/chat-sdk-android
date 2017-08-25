package co.chatsdk.ui;

import android.app.Activity;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.Tab;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.profile.ProfileFragment;
import co.chatsdk.ui.profile.ProfileFragment2;
import co.chatsdk.ui.threads.PrivateThreadsFragment;
import co.chatsdk.ui.threads.PublicThreadsFragment;

public class BaseInterfaceAdapter implements InterfaceAdapter {

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
        return new Tab (R.string.profile, R.drawable.ic_action_user, ProfileFragment.newInstance(NM.currentUser()));
    }

    @Override
    public Activity profileActivity(User user) {
        return null;
    }

    @Override
    public Fragment privateThreadsFragment() {
        return PrivateThreadsFragment.newInstance();
    }

    @Override
    public Fragment publicThreadsFragment() {
        return PublicThreadsFragment.newInstance();
    }

    @Override
    public Fragment contactsFragment() {
        return ContactsFragment.newInstance();
    }

    @Override
    public Fragment profileFragment() {
        return ProfileFragment2.newInstance();
    }



}
