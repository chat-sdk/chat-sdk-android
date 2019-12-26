package sdk.chat.micro.firebase.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import sdk.chat.micro.firebase.service.Path;

public class Ref {

    public static CollectionReference collection(Path path) {
        CollectionReference ref = db().collection(path.first());
        for (int i = 1; i < path.size(); i=i+2) {
            String c1 = path.get(i);
            String c2 = path.get(i+1);
            if (c1 != null && c2 != null) {
                ref = ref.document(c1).collection(c2);
            }
        }
        return ref;
    }

    public static DocumentReference document(Path path) {
        CollectionReference ref = db().collection(path.first());
        for (int i = 1; i < path.size(); i=i+2) {
            String c1 = path.get(i);
            String c2 = path.get(i+1);
            if (c1 != null && c2 != null) {
                ref = ref.document(c1).collection(c2);
            }
        }
        return ref.document(path.last());
    }

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

}
