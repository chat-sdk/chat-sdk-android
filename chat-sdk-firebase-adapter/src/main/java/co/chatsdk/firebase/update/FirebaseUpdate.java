package co.chatsdk.firebase.update;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class FirebaseUpdate {

    public DatabaseReference ref;
    public Object value;

    public FirebaseUpdate(DatabaseReference ref, Object value) {
        this.ref = ref;
        this.value = value;
    }

    public String path () {
        return ref.toString().replace(ref.getRoot().toString(), "");
    }

}
