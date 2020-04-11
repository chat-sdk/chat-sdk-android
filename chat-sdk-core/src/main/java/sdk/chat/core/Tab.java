package sdk.chat.core;


import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;

import sdk.chat.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class Tab {

    public Fragment fragment;
    public String title;
    public Drawable icon;

    public Tab (String title, Drawable icon, Fragment fragment) {
        this.fragment = fragment;
        this.title = title;
        this.icon = icon;
    }

    public Tab (int titleResource, Drawable icon, Fragment fragment, Context context) {
        this(context.getString(titleResource), icon, fragment);
    }

    public Tab (int titleResource, Drawable icon, Fragment fragment) {
        this(titleResource, icon, fragment, ChatSDK.ctx());
    }

}
