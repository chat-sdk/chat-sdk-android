package co.chatsdk.firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import co.chatsdk.core.utils.Executor;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */


public class FirebaseEventListener implements ChildEventListener {

    private Change onChildAdded;
    private Change onChildChanged;
    private Removed onChildRemoved;
    private Change onChildMoved;
    private Error onCancelled;

    public interface Change {
        public void trigger (DataSnapshot snapshot, String s, boolean hasValue);
    }
    public interface Removed {
        public void trigger (DataSnapshot snapshot, boolean hasValue);
    }
    public interface Error {
        public void trigger (DatabaseError error);
    }

    public FirebaseEventListener onChildAdded (Change event) {
        onChildAdded = event;
        return this;
    }

    public FirebaseEventListener onChildChanged (Change event) {
        onChildChanged = event;
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

    private void run (Runnable runnable) {
        Executor.getInstance().execute(runnable);
    }

    @Override
    public void onChildAdded(final DataSnapshot var1, final String var2) {
        if(onChildAdded != null) {
            run(new Runnable() {
                @Override
                public void run() {
                    onChildAdded.trigger(var1, var2, hasValue(var1));
                }
            });
        }
    }

    @Override
    public void onChildChanged(final DataSnapshot var1, final String var2) {
        if(onChildChanged != null) {
            run(new Runnable() {
                @Override
                public void run() {
                    onChildChanged.trigger(var1, var2, hasValue(var1));
                }
            });
        }
    }

    @Override
    public void onChildRemoved(final DataSnapshot var1) {
        if(onChildRemoved != null) {
            run(new Runnable() {
                @Override
                public void run() {
                    onChildRemoved.trigger(var1, hasValue(var1));
                }
            });
        }
    }

    @Override
    public void onChildMoved(final DataSnapshot var1, final String var2) {
        if(onChildMoved != null) {
            run(new Runnable() {
                @Override
                public void run() {
                    onChildMoved.trigger(var1, var2, hasValue(var1));
                }
            });
        }
    }

    @Override
    public void onCancelled(final DatabaseError var1) {
        if(onCancelled != null) {
            run(new Runnable() {
                @Override
                public void run() {
                    onCancelled.trigger(var1);
                }
            });
        }
    }

    private boolean hasValue (DataSnapshot s) {
        return s != null && s.getValue() != null;
    }

}
