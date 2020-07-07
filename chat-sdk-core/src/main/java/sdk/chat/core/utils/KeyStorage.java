package sdk.chat.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import sdk.chat.core.interfaces.IKeyStorage;
import sdk.chat.core.session.ChatSDK;

import static sdk.chat.core.session.ChatSDK.Preferences;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class KeyStorage implements IKeyStorage {

    public static String UsernameKey = "user";
    public static String PasswordKey = "password";

    Context context;

    public KeyStorage (Context context) {
        this.context = context;
    }

    // TODO: Implement this using Keystore
    // http://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
    public void put(String key, String value) {
        SharedPreferences.Editor editor = edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void save(String username, String password) {
        SharedPreferences.Editor editor = edit();
        editor.putString(UsernameKey, username);
        editor.putString(PasswordKey, password);
        editor.apply();
    }

    public String get(String key) {
        return pref().getString(key, null);
    }

    public int getInt(String key) {
        return pref().getInt(key, 0);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        SharedPreferences.Editor editor = ChatSDK.shared().getPreferences().edit();
        editor.remove(UsernameKey);
        editor.remove(PasswordKey);
        editor.apply();
    }

    public SharedPreferences.Editor edit() {
        return pref().edit();
    }

    public SharedPreferences pref() {
        return context.getSharedPreferences(Preferences, Context.MODE_PRIVATE);
    }

}
