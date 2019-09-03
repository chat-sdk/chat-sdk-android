package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by benjaminsmiley-andrews on 18/05/2017.
 */

public class FirebaseReferenceManager {

    private class Value {

        private ChildEventListener cel;
        private ValueEventListener vel;
        private Query ref;

        private Value (Query ref, ChildEventListener listener) {
            this.cel = listener;
            this.ref = ref;
        }

        private Value (Query ref, ValueEventListener listener) {
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

    public void addRef (Query ref, ChildEventListener l) {
        references.put(ref.getRef().toString(), new Value(ref, l));
    }

    public void addRef (Query ref, ValueEventListener l) {
        references.put(ref.getRef().toString(), new Value(ref, l));
    }

    public boolean isOn (Query ref) {
        return references.get(ref.getRef().toString()) != null;
    }

    public void removeListeners (Query ref) {
        if(isOn(ref)) {
            Value v = references.get(ref.getRef().toString());
            v.removeListener();
            references.remove(ref.getRef().toString());
        }
    }

    public void removeAllListeners () {
        Collection<Value> values = references.values();
        for(Value v : values) {
            v.removeListener();
        }
    }

}