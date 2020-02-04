package firestream.chat.firebase.generic;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Generic {

    public static class HashMapStringObject extends HashMap<String, Object> {
    }

    public static GenericTypeIndicator<HashMap<String, Object>> hashMapStringObject() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }

}
