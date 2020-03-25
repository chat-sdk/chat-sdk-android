package co.chatsdk.core.utils;

import android.content.Context;
import android.provider.Settings;

import co.chatsdk.core.session.ChatSDK;

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
        String deviceName = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
        return deviceName != null && deviceName.equals(name);
    }

}
