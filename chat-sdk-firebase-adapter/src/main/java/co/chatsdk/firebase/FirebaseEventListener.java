package co.chatsdk.firebase;

import android.os.AsyncTask;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */


public class FirebaseEventListener implements ChildEventListener, ValueEventListener {

    private Change onChildAdded;
    private Change onChildChanged;
    private Removed onChildRemoved;
    private Change onChildMoved;
    private Error onCancelled;
    private Value onValue;

    public interface Change {
        void trigger (DataSnapshot snapshot, String s, boolean hasValue);
    }
    public interface Removed {
        void trigger (DataSnapshot snapshot, boolean hasValue);
    }
    public interface Error {
        void trigger (DatabaseError error);
    }

    public interface Value {
        void trigger(DataSnapshot snapshot, boolean hasValue);
    }

    public FirebaseEventListener onChildAdded (Change event) {
        onChildAdded = event;
        return this;
    }

    public FirebaseEventListener onChildChanged (Change event) {
        onChildChanged = event;
        return this;
    }

    public FirebaseEventListener onValue(Value event) {
        onValue = event;
        return this;
    }

    public FirebaseEventListener onChildMoved (Change event) {
        onChildMoved = event;
        return this;
    }

    public FirebaseEventListener onChildRemoved (Removed event) {
        onChildRemoved = event;
        return this;
    }

    public FirebaseEventListener onCancelled (Error error) {
        onCancelled = error;
        return this;
    }

    @Override
    public void onChildAdded(final DataSnapshot var1, final String var2) {
        if(onChildAdded != null) {
            AsyncTask.execute(() -> onChildAdded.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onChildChanged(final DataSnapshot var1, final String var2) {
        if(onChildChanged != null) {
            AsyncTask.execute(() -> onChildChanged.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onChildRemoved(final DataSnapshot var1) {
        if(onChildRemoved != null) {
            AsyncTask.execute(() -> onChildRemoved.trigger(var1, hasValue(var1)));
        }
    }

    @Override
    public void onChildMoved(final DataSnapshot var1, final String var2) {
        if(onChildMoved != null) {
            AsyncTask.execute(() -> onChildMoved.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        if(onValue != null) {
            AsyncTask.execute(() -> onValue.trigger(dataSnapshot, hasValue(dataSnapshot)));
        }
    }

    @Override
    public void onCancelled(final DatabaseError var1) {
        if(onCancelled != null) {
            AsyncTask.execute(() -> onCancelled.trigger(var1));
        }
    }

    private boolean hasValue (DataSnapshot s) {
        return s != null && s.getValue() != null;
    }

}
