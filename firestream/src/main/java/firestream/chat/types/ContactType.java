package firestream.chat.types;

import java.util.HashMap;
import java.util.Map;

import firestream.chat.firebase.service.Keys;

public class ContactType extends BaseType {

    /**
     * They have full access rights, can add and remove admins
     */
    public static String Contact = "contact";

    public ContactType(String type) {
        super(type);
    }

    public ContactType(BaseType type) {
        super(type);
    }

    public static ContactType contact() {
        return new ContactType(Contact);
    }

    public Map<String, Object> data () {
        Map<String, Object> data = new HashMap<>();
        data.put(Keys.Type, get());
        return data;
    }

}
