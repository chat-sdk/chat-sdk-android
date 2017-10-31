/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.main;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.core.Tab;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

    protected List<Tab> tabs;

    public PagerAdapterTabs(FragmentManager fm) {
        super(fm);
        tabs = InterfaceManager.shared().a.defaultTabs();
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

    @Override
    public int getPageIconResId(int position) {
        return tabs.get(position).icon;
    }
}
