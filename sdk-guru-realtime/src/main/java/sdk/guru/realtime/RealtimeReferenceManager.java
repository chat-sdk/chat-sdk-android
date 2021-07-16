package sdk.guru.realtime;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by benjaminsmiley-andrews on 18/05/2017.
 */

public class RealtimeReferenceManager {

    protected class Value {

        protected ChildEventListener cel;
        protected ValueEventListener vel;
        protected Query ref;

        protected Value(Query ref, ChildEventListener listener) {
            this.cel = listener;
            this.ref = ref;
        }

        protected Value(Query ref, ValueEventListener listener) {
            this.vel = listener;
            this.ref = ref;
        }

        protected void removeListener() {
            if(cel != null) {
                ref.removeEventListener(cel);
            }
            if(vel != null) {
                ref.removeEventListener(vel);
            }
        }
    }

    protected static RealtimeReferenceManager instance;
    protected Map<String, List<Value>> references = new HashMap<>();

    public static RealtimeReferenceManager shared() {
        if (instance == null) {
            instance = new RealtimeReferenceManager();
        }
        return instance;
    }

    public void addRef(Query ref, ChildEventListener l) {
        listForRef(ref).add(new Value(ref, l));
    }

    public void addRef(Query ref, ValueEventListener l) {
        listForRef(ref).add(new Value(ref, l));
    }

    protected List<Value> listForRef(Query ref) {
        List<Value> list = references.get(ref.getRef().toString());
        if (list == null) {
            list = new ArrayList<>();
            references.put(ref.getRef().toString(), list);
        }
        return list;
    }

    public boolean isOn(Query ref) {
        return references.containsKey(ref.getRef().toString());
    }

    public void removeListeners (Query ref) {
        if(isOn(ref)) {
            for (Value v: listForRef(ref)) {
                v.removeListener();
            }
            references.remove(ref.getRef().toString());
        }
    }

    public void removeAllListeners() {
        Collection<List<Value>> lists = references.values();
        for (List<Value> l: lists) {
            for(Value v : l) {
                v.removeListener();
            }
        }
    }

    public void clear() {
        references.clear();
    }

}