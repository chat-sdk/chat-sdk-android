package sdk.chat.firebase.adapter.utils;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;
import java.util.Map;

public class Generic {
    public class ContactHashMap extends HashMap<String, Long> {
        public ContactHashMap(){}
    }
    static public GenericTypeIndicator<HashMap<String, Long>> contactType() {
        return new GenericTypeIndicator<HashMap<String, Long>>() {};
    }
    public static GenericTypeIndicator<Map<String, Object>> mapStringObject() {
        return new GenericTypeIndicator<Map<String, Object>>() {};
    }

    public static GenericTypeIndicator<Map<String, String>> mapStringString() {
        return new GenericTypeIndicator<Map<String, String>>() {};
    }

//    public static GenericTypeIndicator<Map<String, Map<String, Object>>> mapStringMapStringObject() {
//        return new GenericTypeIndicator<Map<String, Map<String, Object>>>() {};
//    }

    public static GenericTypeIndicator<Map<String, Map<String, Long>>> readReceiptHashMap() {
        return new GenericTypeIndicator<Map<String, Map<String, Long>>>() {};
    }
}
