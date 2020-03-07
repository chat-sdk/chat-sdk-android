package co.chatsdk.core.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.DimenRes;

import co.chatsdk.core.session.ChatSDK;

public class Dimen {

    public static int from(@DimenRes int resourceId) {
        return from(context(), resourceId);
    }

    public static int from(Context context, @DimenRes int resourceId) {
        return Math.round(context.getResources().getDimension(resourceId));
    }

    public static float pxFrom(@DimenRes int resourceId) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, from(context(), resourceId), context().getResources().getDisplayMetrics());
    }

    public static float pxFrom(Context context, @DimenRes int resourceId) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, from(context, resourceId), context.getResources().getDisplayMetrics());
    }

    public static int pxFromAsInt(@DimenRes int resourceId) {
        return pxFromAsInt(context(), resourceId);
    }

    public static int pxFromAsInt(Context context, @DimenRes int resourceId) {
        return Math.round(pxFrom(context, resourceId));
    }

    protected static Context context() {
        return ChatSDK.shared().context();
    }

}
