package sdk.chat.micro.firebase.realtime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sdk.chat.micro.firebase.service.Path;

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
