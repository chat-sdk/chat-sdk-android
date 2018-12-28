package com.raymond.gossipgirl;

import android.content.Context;

import co.chatsdk.ui.manager.BaseInterfaceAdapter;

public class GGInterfaceAdapter extends BaseInterfaceAdapter {

    public GGInterfaceAdapter(Context context) {
        super(context);
    }

    @Override
    public Class getEditProfileActivity() {
        return GGEditProfileActivity.class;
    }

}
