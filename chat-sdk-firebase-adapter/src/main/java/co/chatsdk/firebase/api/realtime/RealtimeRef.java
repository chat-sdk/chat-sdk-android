package co.chatsdk.firebase.api.realtime;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.api.Path;
import co.chatsdk.firebase.api.Ref;

public class RealtimeRef {

    public static DatabaseReference fromPath (Path path) {

        // Get the root path
        String url = ChatSDK.config().firebaseDatabaseUrl;
        DatabaseReference reference = null;
        if (url != null) {
            reference = FirebaseDatabase.getInstance(url).getReference();
        } else {
            reference =  FirebaseDatabase.getInstance().getReference();
        }

        // Build the reference
        for (Ref ref : path.getReferences()) {
            reference = reference.child(ref.getCollection()).child(ref.getId());
        }

        return reference;
    }

}
