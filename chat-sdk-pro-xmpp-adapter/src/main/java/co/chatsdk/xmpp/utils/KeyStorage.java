package co.chatsdk.xmpp.utils;

import android.content.SharedPreferences;

import java.util.HashMap;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class KeyStorage {

    public static String UsernameKey = "user";
    public static String PasswordKey = "password";

    HashMap<String, String> values = new HashMap<>();

    public KeyStorage () {

    }

    // TODO: Implement this using Keystore
    // http://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
    public void put(String key, String value) {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void save(String username, String password) {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.putString(UsernameKey, username);
        editor.putString(PasswordKey, password);
        editor.apply();
    }

    public String get (String key) {
        return ChatSDK.shared().getPreferences().getString(key, null);
    }

    public void clear() {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.remove(UsernameKey);
        editor.remove(PasswordKey);
        editor.commit();
    }


}
