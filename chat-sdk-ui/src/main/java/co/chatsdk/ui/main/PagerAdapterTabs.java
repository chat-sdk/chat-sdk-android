/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.main;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.core.session.ChatSDK;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentPagerAdapter {

    protected List<Tab> tabs;

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);
        tabs = ChatSDK.ui().tabs();
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).title;
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        return tabs.get(position).fragment;
    }

}
