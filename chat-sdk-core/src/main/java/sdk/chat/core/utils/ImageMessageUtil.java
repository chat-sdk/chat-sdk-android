package sdk.chat.core.utils;

import static sdk.chat.core.utils.Device.dpToPx;

import android.content.res.Resources;
import android.util.TypedValue;

import sdk.chat.core.session.ChatSDK;

public class ImageMessageUtil {

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static Size getImageMessageSize(int width, int height) {
        return getImageMessageSize((float) width, (float) height);
    }

    public static Size getImageMessageSize(float width, float height) {
        // Work out the dimensions
        float w = Math.min(getScreenWidth() * 0.8f, dpToPx(ChatSDK.config().imageMessageMaxWidthDp));

        float ar = 1;
        if (width > 0 && height > 0) {
            ar = width / height;
            ar = Math.max(ar, ChatSDK.config().imageMessageMinAR);
            ar = Math.min(ar, ChatSDK.config().imageMessageMaxAR);
        }

        float h = w / ar;

        return new Size(w, h);
    }

}
