package sdk.chat.core.utils;

import android.content.Context;
import android.provider.Settings;

import sdk.chat.core.session.ChatSDK;

public class Device {

    public static boolean honor() {
        return honor(ChatSDK.ctx());
    }

    public static boolean honor(Context context) {
        return named(context, "honor");
    }

    public static boolean nexus() {
        return nexus(ChatSDK.ctx());
    }

    public static boolean nexus(Context context) {
        return named(context, "Nexus 5");
    }

    public static boolean named(String name) {
        return named(ChatSDK.ctx(), name);
    }

    public static boolean named(Context context, String name) {
        String deviceName = name(context);
        return deviceName != null && deviceName.equals(name);
    }

    public static String name(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
    }

}
