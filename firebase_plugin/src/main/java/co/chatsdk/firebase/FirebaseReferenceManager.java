package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by benjaminsmiley-andrews on 18/05/2017.
 */

public class FirebaseReferenceManager {

    private class Value {

        private ChildEventListener cel;
        private ValueEventListener vel;
        private DatabaseReference ref;

        private Value (DatabaseReference ref, ChildEventListener listener) {
            this.cel = listener;
            this.ref = ref;
        }

        private Value (DatabaseReference ref, ValueEventListener listener) {
            this.vel = listener;
            this.ref = ref;
        }

        private void removeListener () {
            if(cel != null) {
                ref.removeEventListener(cel);
            }
            if(vel != null) {
                ref.removeEventListener(vel);
            }
        }

    }

    private static FirebaseReferenceManager instance;
    private HashMap<String, Value> references = new HashMap<>();

    public static FirebaseReferenceManager shared() {
        if (instance == null) {
            instance = new FirebaseReferenceManager();
        }
        return instance;
    }

    public void addRef (DatabaseReference ref, ChildEventListener l) {
        references.put(ref.toString(), new Value(ref, l));
    }

    public void addRef (DatabaseReference ref, ValueEventListener l) {
        references.put(ref.toString(), new Value(ref, l));
    }

    public boolean isOn (DatabaseReference ref) {
        return references.get(ref.toString()) != null;
    }

    public void removeListener (DatabaseReference ref) {
        if(isOn(ref)) {
            Value v = references.get(ref.toString());
            v.removeListener();
            references.remove(ref.toString());
        }
    }

    public void removeAllListeners () {
        Collection<Value> values = references.values();
        for(Value v : values) {
            v.removeListener();
        }
    }

}
