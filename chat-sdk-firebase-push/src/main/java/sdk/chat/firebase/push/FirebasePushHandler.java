package sdk.chat.firebase.push;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.pmw.tinylog.Logger;

import java.util.HashMap;

import io.reactivex.Completable;
import sdk.chat.core.push.AbstractPushHandler;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.guru.common.RX;
import sdk.guru.realtime.RealtimeReferenceManager;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler extends AbstractPushHandler {

    protected boolean enabled = true;

    public FirebasePushHandler () {

        DatabaseReference connectedRef = FirebaseCoreHandler.database().getReference(".info/connected");
        ValueEventListener listener = connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    enabled = false;
                } else {
                    enabled = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
        RealtimeReferenceManager.shared().addRef(connectedRef, listener);
    }

    // Rather than subscribing for one user topic, subscribe to each thread as a new topic
    // Then we can mute notifications by just unsubscribing making the push script easier!

    @Override
    public Completable subscribeToPushChannel(final String channel) {
        return Completable.defer(() -> {
            String hash = hashChannel(channel);
            return Completable.create(emitter -> messaging().subscribeToTopic(hash).addOnSuccessListener(aVoid -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError)).andThen(super.subscribeToPushChannel(hash)).subscribeOn(RX.io());
        });
    }

    @Override
    public Completable unsubscribeToPushChannel(String channel) {
        return Completable.defer(() -> {
            String hash = hashChannel(channel);
            return Completable.create(emitter -> messaging().unsubscribeFromTopic(hash).addOnSuccessListener(aVoid -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError)).andThen(super.unsubscribeToPushChannel(hash)).subscribeOn(RX.io());
        });
    }

    @Override
    public void sendPushNotification (HashMap<String, Object> data) {
        if (data != null) {
            functions().getHttpsCallable("pushToChannels").call(data).continueWith((Continuation<HttpsCallableResult, String>) task -> {
                if(task.getException() != null) {
                    Logger.error(task.getException());
                }
                else {
                    Logger.debug(task.getResult().getData().toString());
                }
                return null;
            });
        }
    }

    public static FirebaseFunctions functions () {
        if (FirebasePushModule.config().firebaseFunctionsRegion != null) {
            return FirebaseFunctions.getInstance(FirebaseCoreHandler.app(), FirebasePushModule.config().firebaseFunctionsRegion);
        } else {
            return FirebaseFunctions.getInstance(FirebaseCoreHandler.app());
        }
    }

    public static FirebaseMessaging messaging () {
        return FirebaseMessaging.getInstance();
    }

    public String hashChannel(String channel) throws Exception {
        if (!FirebaseModule.config().enableCompatibilityWithV4) {
            return md5(channel);
        } else {
            return super.hashChannel(channel);
        }
    }

    public boolean enabled() {
        return enabled;
    }

}
