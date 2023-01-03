package sdk.chat.core.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.session.ChatSDK;

public class Device {

    public static boolean honor() {
        return named("LLD-L31");
    }

    public static boolean nexus() {
        return named("Nexus 5");
    }

    public static boolean galaxy() {
        return named("SM-A217F");
    }

    public static boolean pixel() {
        return named("Pixel 6");
    }

    public static boolean named(String name) {
        String deviceName = name();
        return deviceName != null && deviceName.equals(name);
    }

    public static String name() {
        return android.os.Build.MODEL;
    }

    public static boolean isPortrait(Context context) {
        if (context == null) {
            context = ChatSDK.ctx();
        }
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static boolean isPortrait() {
        return isPortrait(null);
    }

    public static int dpToPxInt(float dp) {
        return Math.round(dpToPx(dp));
    }

    public static float dpToPx(float dp) {
        Resources r = ChatSDK.ctx().getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics());
    }

    public static float pxToDp(float px) {
        Resources r = ChatSDK.ctx().getResources();
        return px / ((float) r.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static List<Float> pxToDp(List<Float> px) {
        List<Float> dp = new ArrayList<>();
        for (Float p : px) {
            dp.add(pxToDp(p));
        }
        return dp;
    }

    public static int spToPxInt(float sp) {
        return Math.round(spToPx(sp));
    }

    public static float spToPx(float sp) {
        Resources r = ChatSDK.ctx().getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                r.getDisplayMetrics());
    }

    public static int pxToSp(int px) {
        Resources r = ChatSDK.ctx().getResources();
        float scaledDensity = r.getDisplayMetrics().scaledDensity;
        return Math.round(px/scaledDensity);
    }

    public static int dpToPx(int dp) {
        return Math.round(dpToPx((float) dp));
    }

    public static int spToPx(int sp) {
        return Math.round(spToPx((float) sp));
    }


}
