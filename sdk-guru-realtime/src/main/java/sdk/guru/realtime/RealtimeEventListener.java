package sdk.guru.realtime;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews childOn 10/05/2017.
 */


public class RealtimeEventListener implements ChildEventListener, ValueEventListener {

    private Change onChildAdded;
    private Change onChildChanged;
    private Removed onChildRemoved;
    private Change onChildMoved;
    private Error onCancelled;
    private Value onValue;

    public interface Change {
        void trigger(DataSnapshot snapshot, String s, boolean hasValue);
    }
    public interface Removed {
        void trigger(DataSnapshot snapshot, boolean hasValue);
    }
    public interface Error {
        void trigger(DatabaseError error);
    }

    public interface Value {
        void trigger(DataSnapshot snapshot, boolean hasValue);
    }

    public RealtimeEventListener onChildAdded (Change event) {
        onChildAdded = event;
        return this;
    }

    public RealtimeEventListener onChildChanged (Change event) {
        onChildChanged = event;
        return this;
    }

    public RealtimeEventListener onValue(Value event) {
        onValue = event;
        return this;
    }

    public RealtimeEventListener onChildMoved (Change event) {
        onChildMoved = event;
        return this;
    }

    public RealtimeEventListener onChildRemoved (Removed event) {
        onChildRemoved = event;
        return this;
    }

    public RealtimeEventListener onCancelled (Error error) {
        onCancelled = error;
        return this;
    }

    @Override
    public void onChildAdded(final DataSnapshot var1, final String var2) {
        if(onChildAdded != null) {
            RX.db().scheduleDirect(() -> onChildAdded.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onChildChanged(final DataSnapshot var1, final String var2) {
        if(onChildChanged != null) {
            RX.db().scheduleDirect(() -> onChildChanged.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onChildRemoved(final DataSnapshot var1) {
        if(onChildRemoved != null) {
            RX.db().scheduleDirect(() -> onChildRemoved.trigger(var1, hasValue(var1)));
        }
    }

    @Override
    public void onChildMoved(final DataSnapshot var1, final String var2) {
        if(onChildMoved != null) {
            RX.db().scheduleDirect(() -> onChildMoved.trigger(var1, var2, hasValue(var1)));
        }
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        if(onValue != null) {
            RX.db().scheduleDirect(() -> onValue.trigger(dataSnapshot, hasValue(dataSnapshot)));
        }
    }

    @Override
    public void onCancelled(final DatabaseError var1) {
        if(onCancelled != null) {
            RX.db().scheduleDirect(() -> onCancelled.trigger(var1));
        }
    }

    private boolean hasValue (DataSnapshot s) {
        return s != null && s.exists() && s.getValue() != null;
    }

}
