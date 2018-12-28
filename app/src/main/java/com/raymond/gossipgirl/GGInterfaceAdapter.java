package com.raymond.gossipgirl;

import android.content.Context;

import java.util.List;

import co.chatsdk.core.Tab;
import co.chatsdk.firebase.nearby_users.NearbyUsersFragment;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

public class GGInterfaceAdapter extends BaseInterfaceAdapter {

    public GGInterfaceAdapter(Context context) {
        super(context);
    }

    public Tab neabyUsersTab() {
        return new Tab(R.string.nearby_users, R.drawable.nearby_users, new NearbyUsersFragment());
    }

    @Override
    public List<Tab> defaultTabs() {
        List<Tab> tabs = super.defaultTabs();
        tabs.add(0, neabyUsersTab());
        return tabs;
    }

    @Override
    public Class getEditProfileActivity() {
        return GGEditProfileActivity.class;
    }

}
