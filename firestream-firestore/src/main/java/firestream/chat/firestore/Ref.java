package firestream.chat.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import firestream.chat.firebase.service.Path;
import firestream.chat.namespace.Fire;


public class Ref {

    public static CollectionReference collection(Path path) {
        Object ref = referenceFromPath(path);
        if (ref instanceof CollectionReference) {
            return (CollectionReference) ref;
        } else {
            Fire.internal().debug((Fire.internal().context().getString(R.string.error_mismatched_col_reference)));
            return null;
        }
    }

    public static DocumentReference document(Path path) {
        Object ref = referenceFromPath(path);
        if (ref instanceof DocumentReference) {
            return (DocumentReference) ref;
        } else {
            Fire.internal().debug(Fire.internal().context().getString(R.string.error_mismatched_doc_reference));
            return null;
        }
    }

    public static Object referenceFromPath(Path path) {
        Object ref = db().collection(path.first());

        for (int i = 1; i < path.size(); i++) {
            String component = path.get(i);

            if (ref instanceof DocumentReference) {
                ref = ((DocumentReference) ref).collection(component);
            } else {
                ref = ((CollectionReference) ref).document(component);
            }
        }
        return ref;
    }

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

}
