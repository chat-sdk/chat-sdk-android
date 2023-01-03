package sdk.chat.firebase.adapter.update;

import com.google.firebase.database.DatabaseReference;

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
