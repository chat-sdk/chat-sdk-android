/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.adapters;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.astuetz.PagerSlidingTabStrip;

import co.chatsdk.ui.fragments.ContactsFragment;
import co.chatsdk.ui.threads.PrivateThreadsFragment;

import co.chatsdk.ui.R;

import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.profile.ProfileFragment;
import co.chatsdk.ui.threads.PublicThreadsFragment;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    protected BaseFragment[] fragments;
    private String[] titles;
    private int[] icons;

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);

        fragments = new BaseFragment[] {
                PrivateThreadsFragment.newInstance(),
                PublicThreadsFragment.newInstance(),
                ContactsFragment.newInstance(),
                ProfileFragment.newInstance()
        };

        titles = new String [] {
                "Conversations",
                "Chat Rooms",
                "Contacts",
                "Profile"
        };

        icons = new int[] {
                R.drawable.ic_action_private,
                R.drawable.ic_action_public,
                R.drawable.ic_action_contacts,
                R.drawable.ic_action_user
        };
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public BaseFragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getPageIconResId(int position) {
        return icons[position];
    }
}
