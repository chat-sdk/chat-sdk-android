package co.chatsdk.core.utils;

import android.content.Context;

import androidx.annotation.DimenRes;

import co.chatsdk.core.session.ChatSDK;

public class Dimen {

    public static int from(@DimenRes int resourceId) {
        return from(ChatSDK.shared().context(), resourceId);
    }

    public static int from(Context context, @DimenRes int resourceId) {
        return Math.round(context.getResources().getDimension(resourceId));
    }

}
