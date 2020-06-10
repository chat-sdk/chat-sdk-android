package sdk.guru.realtime;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;

public class Generic {

    public static class HashMapStringObject extends HashMap<String, Object> {
    }

    public static GenericTypeIndicator<HashMap<String, Object>> hashMapStringObject() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }

}
