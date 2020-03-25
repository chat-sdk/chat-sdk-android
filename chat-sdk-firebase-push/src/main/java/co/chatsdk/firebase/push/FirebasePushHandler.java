package co.chatsdk.firebase.push;

import com.google.android.gms.tasks.Continuation;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.pmw.tinylog.Logger;

import java.util.HashMap;

import co.chatsdk.core.push.AbstractPushHandler;
import co.chatsdk.firebase.FirebaseCoreHandler;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler extends AbstractPushHandler {

    public FirebasePushHandler () {

    }

    // Rather than subscribing for one user topic, subscribe to each thread as a new topic
    // Then we can mute notifications by just unsubscribing making the push script easier!

    @Override
    public Completable subscribeToPushChannel(String channel) {
        return Completable.create(emitter -> messaging().subscribeToTopic(channel).addOnSuccessListener(aVoid -> {
            emitter.onComplete();
        }).addOnFailureListener(emitter::onError)).andThen(super.subscribeToPushChannel(channel)).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable unsubscribeToPushChannel(String channel) {
        return Completable.create(emitter -> messaging().unsubscribeFromTopic(channel).addOnSuccessListener(aVoid -> {
            emitter.onComplete();
        }).addOnFailureListener(emitter::onError)).andThen(super.unsubscribeToPushChannel(channel)).subscribeOn(Schedulers.io());
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

}
