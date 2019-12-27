package firefly.sdk.chat.firebase.generic;

import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;

public class GenericTypes {

    public static GenericTypeIndicator<HashMap<String, Object>> meta() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }

    public static GenericTypeIndicator<HashMap<String, Object>> listEvent() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }

    public static GenericTypeIndicator<HashMap<String, Object>> messageBody() {
        return new GenericTypeIndicator<HashMap<String, Object>>() {};
    }

}
