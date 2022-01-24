package sdk.chat.waka;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.Map;

import sdk.chat.core.dao.User;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;

public class V4V5UserWrapper extends UserWrapper {
    public V4V5UserWrapper(FirebaseUser authData) {
        super(authData);
    }

    public V4V5UserWrapper(User model) {
        super(model);
    }

    public V4V5UserWrapper(DataSnapshot snapshot) {
        super(snapshot);
    }

    public V4V5UserWrapper(String entityID) {
        super(entityID);
    }

    protected Map<String, Object> serialize() {
        Map<String, Object> data = super.serialize();

        return data;
    }

    public void deserializeMeta(Map<String, Object> value, boolean replaceLocal){
        super.deserializeMeta(value, replaceLocal);
    }
}
