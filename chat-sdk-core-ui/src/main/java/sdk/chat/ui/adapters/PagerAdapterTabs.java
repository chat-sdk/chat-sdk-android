/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import sdk.chat.core.Tab;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by itzik on 6/16/2014.
 */
public class PagerAdapterTabs extends FragmentStateAdapter {

    protected List<Tab> tabs;

    public PagerAdapterTabs(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        tabs = ChatSDK.ui().tabs();
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return tabs.get(position).fragment;
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }
}
