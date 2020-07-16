package sdk.chat.firebase.blocking;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

import sdk.chat.core.base.AbstractBlockingHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.realtime.RealtimeEventListener;
import sdk.chat.firebase.adapter.FirebasePaths;
import io.reactivex.Completable;
import sdk.guru.realtime.RealtimeReferenceManager;

import static sdk.chat.firebase.adapter.FirebasePaths.BlockedPath;

/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseBlockingHandler extends AbstractBlockingHandler {

    private ArrayList<String> blockedUserEntityIDs = new ArrayList<>();

    public FirebaseBlockingHandler () {

        ChatSDK.hook().addHook(Hook.sync(data -> {
                on();
        }), HookEvent.DidAuthenticate);
        ChatSDK.hook().addHook(Hook.sync(data -> {
            off();
        }), HookEvent.WillLogout);
    }

    protected void on() {
        DatabaseReference ref = ref();
        ChildEventListener listener = ref.addChildEventListener(new RealtimeEventListener().onChildAdded((snapshot, s, hasValue) -> {
            if (hasValue) {
                if (!blockedUserEntityIDs.contains(snapshot.getKey())) {
                    blockedUserEntityIDs.add(snapshot.getKey());
                }
            }
        }).onChildRemoved((snapshot, hasValue) -> {
            if (hasValue) {
                blockedUserEntityIDs.remove(snapshot.getKey());
            }
        }));
        RealtimeReferenceManager.shared().addRef(ref, listener);
    }

    protected void off() {
        RealtimeReferenceManager.shared().removeListeners(ref());
    }

    @Override
    public Completable blockUser (final String userEntityID) {
        return Completable.create(e -> {

            HashMap<String, String> data = new HashMap<>();
            data.put(Keys.UID, userEntityID);

            this.ref().child(userEntityID).setValue(data, (error, ref) -> {
                if (error == null) {
                    e.onComplete();
                } else {
                    e.onError(new Throwable(error.getMessage()));
                }
            });
        });
    }

    @Override
    public Completable unblockUser (final String userEntityID) {
        return Completable.create(e -> {
            this.ref().child(userEntityID).removeValue((error, ref) -> {
                if (error == null) {
                    e.onComplete();
                } else {
                    e.onError(new Throwable(error.getMessage()));
                }
            });
        });
    }

    @Override
    public Boolean isBlocked (String userEntityID) {
        return this.blockedUserEntityIDs.contains(userEntityID);
    }

    @Override
    public boolean blockingSupported () {
        return true;
    }

    private DatabaseReference ref () {
        return FirebasePaths.userRef(ChatSDK.currentUser().getEntityID()).child(BlockedPath);
    }

}
