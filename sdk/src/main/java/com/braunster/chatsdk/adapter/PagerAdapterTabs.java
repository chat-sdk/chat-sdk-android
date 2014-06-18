package com.braunster.chatsdk.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.braunster.chatsdk.fragments.BaseFragment;
import com.braunster.chatsdk.fragments.ContactsFragment;
import com.braunster.chatsdk.fragments.ConversationsFragment;
import com.braunster.chatsdk.fragments.ProfileFragment;
import com.braunster.chatsdk.fragments.ThreadsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentPagerAdapter {

    private final String[] TITLES = { "Profile", "Chat Rooms", "Contacts", "Conversations"};

    private final BaseFragment[] FRAGMENTS = new BaseFragment[] {ProfileFragment.newInstance(), ThreadsFragment.newInstance(), ContactsFragment.newInstance(), ConversationsFragment.newInstance()};

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public BaseFragment getItem(int position) {
        return FRAGMENTS[position];
    }

}
