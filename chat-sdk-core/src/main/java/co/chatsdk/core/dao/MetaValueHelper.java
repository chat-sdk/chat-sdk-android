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

}
