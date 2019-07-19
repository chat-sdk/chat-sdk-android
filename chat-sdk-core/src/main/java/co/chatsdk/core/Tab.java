package co.chatsdk.core;


import android.content.Context;

import androidx.fragment.app.Fragment;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class Tab {

    public Fragment fragment;
    public String title;
    public int icon;

    public Tab (String title, int icon, Fragment fragment) {
        this.fragment = fragment;
        this.title = title;
        this.icon = icon;
    }

    public Tab (int titleResource, int icon, Fragment fragment, Context context) {
        this(context.getString(titleResource), icon, fragment);
    }

    public Tab (int titleResource, int icon, Fragment fragment) {
        this(titleResource, icon, fragment, ChatSDK.shared().context());
    }

}
