package firestream.chat.realtime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import firestream.chat.firebase.service.Path;

public class Ref {

    public static DatabaseReference get(Path path){
        DatabaseReference ref = db().getReference(path.first());
        for (int i = 1; i < path.size(); i++) {
            ref = ref.child(path.get(i));
        }
        return ref;
    }

    public static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

}
