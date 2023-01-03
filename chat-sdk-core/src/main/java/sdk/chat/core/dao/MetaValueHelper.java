package sdk.chat.core.dao;

import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ben on 5/17/18.
 */

public class MetaValueHelper {

    public static <V extends  Object, T extends MetaValue<V>> T metaValueForKey(String key, List<T> values) {

        if (values != null && key != null) {

            // Check for duplicates
            HashMap<String, T> hash = new HashMap<>();
            for (T value: values) {
                if (hash.get(value.getKey()) == null) {
                    hash.put(value.getKey(), value);
                } else {
                    Logger.debug("Duplicate");
                }
            }

            return hash.get(key);

//            for (T value : values) {
//                if (value.getKey() != null && key != null) {
//                    if (value.getKey().equals(key)) {
//                        return value;
//                    }
//                }
//            }
        }
        return null;
    }

    public static <V extends Object, T extends MetaValue<V>> boolean isEqual(T metaValue, V value) {
        return metaValue != null && metaValue.getValue() != null && metaValue.getValue().equals(value);
    }

    public static String toString(Object value) {
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
        else if (value != null) {
            return value.toString();
        } else {
            return "";
        }
    }

    public static Object toObject(String value) {
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
