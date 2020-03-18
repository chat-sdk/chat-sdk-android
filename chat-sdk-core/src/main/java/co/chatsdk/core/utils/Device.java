package co.chatsdk.core.utils;

import android.content.Context;
import android.provider.Settings;

import co.chatsdk.core.session.ChatSDK;

public class Device {

    public static boolean honor() {
        return honor(ChatSDK.shared().context());
    }

    public static boolean honor(Context context) {
        return named(context, "honor");
    }

    public static boolean nexus() {
        return nexus(ChatSDK.shared().context());
    }

    public static boolean nexus(Context context) {
        return named(context, "Nexus 5");
    }

    public static boolean named(String name) {
        return Settings.Secure.getString(ChatSDK.shared().context().getContentResolver(), "bluetooth_name").equals(name);
    }

    public static boolean named(Context context, String name) {
        return Settings.Secure.getString(context.getContentResolver(), "bluetooth_name").equals(name);
    }

}
