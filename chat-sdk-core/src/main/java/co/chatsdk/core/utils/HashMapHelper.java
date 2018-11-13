package co.chatsdk.core.utils;

import java.util.HashMap;
import java.util.Map;

public class HashMapHelper {

    public static HashMap<String, Object> expand(Map<String, Object> flatMap) {

        HashMap<String, Object> expandedMap = new HashMap<>();

        for (String key : flatMap.keySet()) {
            String[] split = key.split("/");
            Object value = flatMap.get(key);

            // In this case, we have a composite key i.e. A/B/C which needs to be expanded
            if (split.length > 1) {

                // Split the key into the first part A and child part B/C
                String parentKey = split[0];
                String childKey = key.replace(parentKey + "/", "");

                // Make a new child map which would contain the value keyed by the child key
                // If the map already exists it becomes the child map, otherwise the child map is simply made new
                HashMap<String, Object> childMap = expandedMap.containsKey(parentKey) ? (HashMap) expandedMap.get(parentKey) : new HashMap<>();

                //Here the value and new string is inserted into the child map
                childMap.put(childKey, value);
                //The child map itself is now expanded. This will repeat until split.length =1,
                //at which time the else function below will be triggered.
                expandedMap.put(parentKey, expand(childMap));
            }
            else{
                expandedMap.put(key, value);
            }
        }
        return expandedMap;
    }

    public static HashMap<String, Object> flatten (Map<String, Object> map) {
        return flatten(map, null);
        //This function loops on itself. This is the initial part of it. The other flatten function with an extra variable is needed when the function begins looping on itself.
    }

    public static HashMap<String, Object> flatten(Map<String, Object> map, String previousKey) {
        HashMap<String, Object> outputMap = new HashMap<>();

        //Here we loop over the keyset, and retrieve each value for that.
        for (String key : map.keySet()) {
            Object value = map.get(key);

            //Here the key is determined based on whether it is a new key or a nested key.
            String newKey = previousKey == null ? key : previousKey + "/" + key;

            //If the value is a hashmap, the function loops on itself, otherwise the value is placed into the final map.
            if (value instanceof HashMap) {
                outputMap.putAll(flatten((HashMap) value, newKey));
            }
            else if (value instanceof String) {
                outputMap.put(newKey, (String) value);
            }
        }
        return outputMap;
    }


}
