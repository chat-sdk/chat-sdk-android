package co.chatsdk.core.dao;

import java.util.ArrayList;

/**
 * Created by ben on 5/17/18.
 */

public class MetaValueHelper {

    public static MetaValue metaValueForKey (String key, ArrayList<MetaValue> values) {
        if (values != null) {
            for (MetaValue value : values) {
                if (value.getKey() != null && key != null) {
                    if (value.getKey().equals(key)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    public static String toString (Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        }
        else if (value instanceof Long) {
            return Long.toString((Long) value);
        }
        else if (value instanceof Double) {
            return Double.toString((Double) value);
        }
        else if (value instanceof Float) {
            return Float.toString((Float) value);
        }
        else {
            return value.toString();
        }
    }

    public static Object toObject (String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (Exception e) {
            try {
                return Long.parseLong(value);
            }
            catch (Exception e1) {
                try {
                    return Double.parseDouble(value);
                }
                catch (Exception e2) {
                    try {
                        return Float.parseFloat(value);
                    }
                    catch (Exception e3) {
                        return value;
                    }
                }
            }
        }
    }

}
