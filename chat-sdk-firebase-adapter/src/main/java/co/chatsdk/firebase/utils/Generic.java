package co.chatsdk.firebase.utils;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;

public class Generic {
    public class ContactHashMap extends HashMap<String, Long> {
        public ContactHashMap(){}
    }
    static public GenericTypeIndicator<HashMap<String, Long>> contactType() {
        return new GenericTypeIndicator<HashMap<String, Long>>() {};
    }
    public static GenericTypeIndicator<HashMap<String, Object>> hashMapStringObject() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }
    public static GenericTypeIndicator<HashMap<String, HashMap<String, Long>>> readReceiptHashMap() {
        return new GenericTypeIndicator<HashMap<String, HashMap<String, Long>>>() {};
    }
}
