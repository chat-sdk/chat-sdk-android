package sdk.chat.core.utils;

import android.content.Context;
import android.content.res.Configuration;

import sdk.chat.core.session.ChatSDK;

public class Device {

    public static boolean honor() {
        return named("Chairman Mao");
    }

    public static boolean nexus() {
        return named("Nexus 5");
    }

    public static boolean galaxy() {
        return named("Ben's Galaxy A21s");
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

}
