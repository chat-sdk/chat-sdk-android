package sdk.guru.realtime;

import com.google.firebase.database.DataSnapshot;

import sdk.guru.common.EventType;

public class DocumentChange {

    protected DataSnapshot snapshot;
    protected EventType type;

    public DocumentChange(DataSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public DocumentChange(DataSnapshot snapshot, EventType type) {
        this.snapshot = snapshot;
        this.type = type;
    }

    public DataSnapshot getSnapshot() {
        return snapshot;
    }

    public EventType getType() {
        return type;
    }

}
