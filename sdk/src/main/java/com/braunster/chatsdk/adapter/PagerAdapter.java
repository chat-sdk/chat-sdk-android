package com.braunster.chatsdk.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.braunster.chatsdk.fragments.ContactsFragment;
import com.braunster.chatsdk.fragments.ConversationsFragment;
import com.braunster.chatsdk.fragments.ProfileFragment;
import com.braunster.chatsdk.fragments.ThreadsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = { "Profile", "Chat Rooms", "Contacts", "Conversations"};

    private final Fragment[] FRAGMENTS = new Fragment[] {ProfileFragment.newInstance(), ThreadsFragment.newInstance(), ContactsFragment.newInstance(), ConversationsFragment.newInstance()};

    public PagerAdapter(FragmentManager fm) {
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
    public Fragment getItem(int position) {
        return FRAGMENTS[position];
    }

}
