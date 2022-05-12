package sdk.chat.core.utils;

import android.text.Editable;

/**
 * Created by ben on 9/4/17.
 */

public class StringChecker {

    public static boolean isNullOrEmpty (String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNullOrEmpty (Editable editable) {
        return editable == null || editable.toString().isEmpty();
    }

    public static boolean areEqual(String s1, String s2) {
        if (s1 != null && s2 != null) {
            return s1.equals(s2);
        }
        return s1 == null && s2 == null;
    }

}
