/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package sdk.chat.firebase.adapter.wrappers;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.SingleSource;
import sdk.chat.core.avatar.HashAvatarGenerator;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.defines.Availability;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.HashMapHelper;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.chat.firebase.adapter.FirebaseEntity;
import sdk.guru.common.Optional;
import sdk.guru.realtime.RealtimeEventListener;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.guru.realtime.RealtimeReferenceManager;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.utils.FirebaseRX;
import sdk.chat.firebase.adapter.utils.Generic;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.guru.common.RX;



public class UserWrapper {

    private User model;

    public static UserWrapper initWithAuthData(FirebaseUser authData){
        return new UserWrapper(authData);
    }

    public static UserWrapper initWithModel(User user){
        return new UserWrapper(user);
    }

    private UserWrapper(FirebaseUser authData){
        model = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, authData.getUid());
        updateUserFromAuthData(authData);


    }

    public UserWrapper(User model) {
        this.model = model;
    }
    
    public UserWrapper(DataSnapshot snapshot){
        this(snapshot.getKey());
        deserializeMeta(snapshot.child(Keys.Meta).getValue(Generic.mapStringObject()));
    }

    public UserWrapper(String entityID) {
        model = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
    }

    /**
     * Note - Change was removing of online values as set online and online time.
     * * * * */
    private void updateUserFromAuthData(FirebaseUser authData){
        Logger.debug("updateUserFromAuthData");

        model.setEntityID(authData.getUid());

        String name = authData.getDisplayName();
        String email = authData.getEmail();
        String phoneNumber = authData.getPhoneNumber();

        // Setting the name.
        if (StringChecker.isNullOrEmpty(model.getName()) && !StringChecker.isNullOrEmpty(name)) {
            model.setName(name);
        }

        // Setting the email.
        if (StringChecker.isNullOrEmpty(model.getEmail()) && !StringChecker.isNullOrEmpty(email)) {
            model.setEmail(email);
        }

        if (StringChecker.isNullOrEmpty(model.getPhoneNumber()) && !StringChecker.isNullOrEmpty(phoneNumber)) {
            model.setPhoneNumber(phoneNumber);
        }

        String profileURL = model.getAvatarURL();

        if(StringChecker.isNullOrEmpty(profileURL)) {
            if (authData.getPhotoUrl() != null) {
                profileURL = authData.getPhotoUrl().toString();
            }
        }
        if (StringChecker.isNullOrEmpty(profileURL)) {
            profileURL = ChatSDK.config().defaultUserAvatarURL;
        }
        if (StringChecker.isNullOrEmpty(profileURL)) {
            profileURL = ChatSDK.ui().getAvatarGenerator().getAvatarURL(model);
        }
        if (!StringChecker.isNullOrEmpty(profileURL)) {
            model.setAvatarURL(profileURL);
        }
        model.update();


        if (StringChecker.isNullOrEmpty(model.getAvailability())) {
            model.setAvailability(Availability.Available);
        }

        // Test to see if the avatar is valid
        ChatSDK.events().disposeOnLogout(ImageUtils.bitmapForURL(profileURL).subscribe((bitmap, throwable) -> {
            if (throwable != null) {
                model.setAvatarURL(new HashAvatarGenerator().getAvatarURL(model));
//                push().subscribe(ChatSDK.events());
            }
        }));

    }

//    public Completable once(){
//        return Completable.create(e -> {
//
//            final DatabaseReference ref = ref();
//
//            ref.addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
//                if(hasValue) {
//                    deserialize((Map<String, Object>) snapshot.getValue());
//                }
//
//                e.onComplete();
//            }).onCancelled(error -> {
//                e.onError(error.toException());
//            }));
//
//        }).subscribeOn(RX.firebaseIO());
//    }

    public Completable metaOn() {
        return Completable.create(emitter -> {
            final DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());
            if (RealtimeReferenceManager.shared().isOn(userMetaRef)) {
                emitter.onComplete();
            } else {
                ValueEventListener listener = userMetaRef.addValueEventListener(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
                    if (hasValue && snapshot.getValue() instanceof Map) {
                        deserializeMeta(snapshot.getValue(Generic.mapStringObject()));
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable("User doesn't exist"));
                    }
                }).onCancelled(error -> {
                    emitter.onError(error.toException());
                }));
                RealtimeReferenceManager.shared().addRef(userMetaRef, listener);
            }
        }).subscribeOn(RX.io());
    }

    public void metaOff() {
        DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(userMetaRef);
    }

    public void deserializeMeta(Map<String, Object> value){
        if (value != null) {
            Map<String, String> oldData = model.metaMap();

            // Expand
            Map<String, Object> newData = HashMapHelper.flatten(value);

            // Updating the old bundle
            for (String key : newData.keySet()) {
                if (oldData.get(key) == null || !oldData.get(key).equals(newData.get(key))) {
                    oldData.put(key, newData.get(key).toString());
                }
            }

            model.setMetaMap(oldData);

        }
    }

    public Completable on() {
        return Completable.mergeArray(onlineOn(), metaOn());
    }

    public Completable onlineOn() {
        return Completable.create(emitter -> {
            DatabaseReference ref = FirebasePaths.userOnlineRef(model.getEntityID());
            if (RealtimeReferenceManager.shared().isOn(ref)) {
                emitter.onComplete();
            } else {
                ValueEventListener listener = ref.addValueEventListener(new RealtimeEventListener().onValue((snapshot, hasValue) -> {

                    Boolean available = false;
                    if(hasValue) {
                        Boolean value = snapshot.getValue(Boolean.class);
                        if (value != null) {
                            available = value;
                        }
                    }

                    model.setIsOnline(available);
                    emitter.onComplete();
                }).onCancelled(error -> {
                    emitter.onError(error.toException());
                }));

                RealtimeReferenceManager.shared().addRef(ref, listener);
            }
        }).subscribeOn(RX.io());
    }

    public Single<Map<String, Object>> dataOnce() {
        return Single.create((SingleOnSubscribe<Map<String, Object>>) emitter -> {
            final DatabaseReference ref = metaRef();

            ref.addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    emitter.onSuccess(snapshot.getValue(Generic.mapStringObject()));
                }
                emitter.onSuccess(new HashMap<>());
            }).onCancelled(error -> {
                emitter.onError(error.toException());
            }));

        }).subscribeOn(RX.io());
    }

    public void off() {
        metaOff();
        onlineOff();
    }

    public void onlineOff () {
        DatabaseReference ref = FirebasePaths.userOnlineRef(model.getEntityID());
        RealtimeReferenceManager.shared().removeListeners(ref);
    }

    Map<String, Object> serialize() {
        Map<String, Object> values = new HashMap<>();

        // Don't push availability to Firebase
        HashMap<String, String> metaMap = new HashMap<>(model.metaMap());
        metaMap.put(Keys.NameLowercase, model.getName() != null ? model.getName().toLowerCase() : "");

        // Expand
        Map<String, Object> expandedMetaMap = HashMapHelper.expand(metaMap);

        values.put(Keys.Meta, expandedMetaMap);
        values.put(Keys.LastOnline, ServerValue.TIMESTAMP);

        return values;
    }

    /**
     * Reads are cheaper than writes, so read from the user first to see if
     * the data is different to the local data. If it's not, then push the data
     * @return completable
     */
    public Completable push() {
        return push(false);
    }

    public Completable push(boolean force) {
        return Single.defer((Callable<SingleSource<Optional<Map<String, Object>>>>) () -> {
            if (!force) {
                return dataOnce().map(Optional::new);
            }
            return Single.just(new Optional<>());
        }).flatMapCompletable(mapOptional -> {

            Completable completable = Completable.create(emitter -> ref().updateChildren(serialize(), (firebaseError, firebase) -> {
                if (firebaseError == null) {
                    emitter.onComplete();
                } else {
                    emitter.onError(firebaseError.toException());
                }
            })).andThen(updateFirebaseUser())
                    .andThen(FirebaseEntity.pushUserMetaUpdated(model.getEntityID()))
                    .subscribeOn(RX.io());

            if (!mapOptional.isEmpty()) {
                boolean needsUpdate = !new HashSet<>(mapOptional.get().values()).equals(new HashSet<>(model.metaMap().values()));
                if (needsUpdate && !FirebaseModule.config().disableClientProfileUpdate) {
                    return completable;
                } else {
                    deserializeMeta(mapOptional.get());
                }
                return Completable.complete();
            } else {
                return completable;
            }
        });
    }

    public Completable updateFirebaseUser() {
        return Completable.create(e -> {

            final FirebaseUser user = FirebaseCoreHandler.auth().getCurrentUser();

            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(model.getName());

            if(model.getAvatarURL() != null && model.getAvatarURL().length() > 0) {
                builder.setPhotoUri(Uri.parse(model.getAvatarURL()));
            }

            final UserProfileChangeRequest changeRequest = builder.build();

            user.updateProfile(changeRequest).addOnCompleteListener(task -> e.onComplete());
        }).subscribeOn(RX.io());
    }
    
    public DatabaseReference ref(){
        return FirebasePaths.userRef(model.getEntityID());
    }

    public DatabaseReference metaRef(){
        return FirebasePaths.userMetaRef(model.getEntityID());
    }

    /**
     * Set the user online value to false.
     **/
    public Completable goOffline () {
        return FirebaseRX.remove(FirebasePaths.userOnlineRef(model.getEntityID()));
    }
    
    /**
     * Set the user online value to true.
     * 
     * When firebase disconnect this will be auto change to false.
     **/
    public Completable goOnline() {
        return FirebaseRX.set(FirebasePaths.userOnlineRef(model.getEntityID()), true, true);
    }

    public User getModel () {
        return model;
    }

}
