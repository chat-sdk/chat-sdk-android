package firefly.sdk.chat.firebase.realtime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import firefly.sdk.chat.firebase.service.Path;

public class Ref {

    public static DatabaseReference get(Path path){
        DatabaseReference ref = db().getReference(path.first());
        for (String component: path.getComponents()) {
            ref = ref.child(component);
        }
        return ref;
    }

    public static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

}
