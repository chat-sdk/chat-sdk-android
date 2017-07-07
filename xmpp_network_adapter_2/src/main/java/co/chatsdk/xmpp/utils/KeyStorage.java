package co.chatsdk.xmpp.utils;

import java.util.HashMap;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class KeyStorage {

    public static String UsernameKey = "username";
    public static String PasswordKey = "password";

    HashMap<String, String> values = new HashMap<>();

    public KeyStorage () {

    }

    // TODO: Implement this using Keystore
    // http://www.androidauthority.com/use-android-keystore-store-passwords-sensitive-information-623779/
    public void put(String key, String value) {
        values.put(key, value);
    }

    public String get (String key) {
        return values.get(key);
    }


}
