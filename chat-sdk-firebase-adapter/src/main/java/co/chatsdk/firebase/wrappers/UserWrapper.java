/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.HashMapHelper;
import co.chatsdk.firebase.FirebaseCoreHandler;
import co.chatsdk.firebase.FirebaseEntity;
import co.chatsdk.firebase.FirebaseEventListener;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.FirebaseReferenceManager;
import co.chatsdk.firebase.utils.FirebaseRX;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class UserWrapper {

    private User model;

    public static UserWrapper initWithAuthData(FirebaseUser authData){
        return new UserWrapper(authData);
    }

    public static UserWrapper initWithModel(User user){
        return new UserWrapper(user);
    }
    
    public static UserWrapper initWithSnapshot(DataSnapshot snapshot){
        return new UserWrapper(snapshot);
    }

    public static UserWrapper initWithEntityId(String entityId){
        User model = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityId);
        return initWithModel(model);
    }
    
    private UserWrapper(FirebaseUser authData){
        model = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, authData.getUid());
        updateUserFromAuthData(authData);
    }

    public UserWrapper(User model) {
        this.model = model;
    }
    
    public UserWrapper(DataSnapshot snapshot){
        model = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());
        deserialize((Map<String, Object>) snapshot.getValue());
    }
    
    /**
     * Note - Change was removing of online values as set online and online time.
     * * * * */
    private void updateUserFromAuthData(FirebaseUser authData){
        Timber.v("updateUserFromAuthData");

//        model.setAuthenticationType((Integer) ChatSDK.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey));

        model.setEntityID(authData.getUid());

        String name = authData.getDisplayName();
        String email = authData.getEmail();
        String phoneNumber = authData.getPhoneNumber();
        String profileURL = null;

        if(authData.getPhotoUrl() != null) {
            profileURL = authData.getPhotoUrl().toString();
        }

        // Setting the name.
        if (!StringChecker.isNullOrEmpty(name) && StringChecker.isNullOrEmpty(model.getName())) {
            model.setName(name);
        }
        else {
            model.setName(ChatSDK.config().defaultName);
        }

        // Setting the email.
        if (!StringChecker.isNullOrEmpty(email) && StringChecker.isNullOrEmpty(model.getEmail())) {
            model.setEmail(email);
        }

        if (!StringChecker.isNullOrEmpty(phoneNumber) && StringChecker.isNullOrEmpty(model.getPhoneNumber())) {
            model.setPhoneNumber(phoneNumber);
        }

        if (!StringChecker.isNullOrEmpty(profileURL) && StringChecker.isNullOrEmpty(model.getAvatarURL())) {
            model.setAvatarURL(profileURL);
        }
        else {
            String url = ChatSDK.config().defaultUserAvatarURL;
            model.setAvatarURL(url);
        }

        model.update();
    }

    public Completable once(){
        return Completable.create(e -> {

            final DatabaseReference ref = ref();

            ref.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    deserialize((Map<String, Object>) snapshot.getValue());
                }
                e.onComplete();
            }).onCancelled(error -> {
                e.onError(error.toException());
            }));

        }).subscribeOn(Schedulers.single());
    }

    public Observable<User> metaOn() {
        return Observable.create((ObservableOnSubscribe<User>) e -> {

            metaOff();

            final DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());

            if(FirebaseReferenceManager.shared().isOn(userMetaRef)) {
                e.onNext(model);
            }

            ValueEventListener listener = userMetaRef.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if (hasValue && snapshot.getValue() instanceof Map) {
                    deserializeMeta((Map<String, Object>) snapshot.getValue());
                    e.onNext(model);
                } else {
                    e.onError(new Throwable("User doesn't exist"));
                }
            }));

            FirebaseReferenceManager.shared().addRef(userMetaRef, listener);



        }).subscribeOn(Schedulers.single());
    }


    public void metaOff(){
        DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListeners(userMetaRef);
    }


    void deserialize(Map<String, Object> value){

        if (value != null)
        {
            if (value.containsKey(Keys.Online) && !value.get(Keys.Online).equals("")) {
                Boolean online = (Boolean) value.get(Keys.Online);
                model.setAvailability(online ? Availability.Available : Availability.Unavailable);
            }

            // The entity update is called in the deserializeMeta.
            deserializeMeta((Map<String, Object>) value.get(FirebasePaths.MetaPath));
        }
    }

    void deserializeMeta(Map<String, Object> value){
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
            model.update();

        }
    }

    public Observable<Boolean> onlineOn () {
        onlineOff();
        return Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            DatabaseReference ref = FirebasePaths.userOnlineRef(model.getEntityID());

            ValueEventListener listener = ref.addValueEventListener(new FirebaseEventListener().onValue((snapshot, hasValue) -> {

                Boolean available = false;
                if(hasValue) {
                    available = (boolean) snapshot.getValue();
                }
                model.setIsOnline(available);
                e.onNext(available);
            }));
            FirebaseReferenceManager.shared().addRef(ref, listener);
        }).subscribeOn(Schedulers.single());
    }

    public void onlineOff () {
        DatabaseReference ref = FirebasePaths.userOnlineRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListeners(ref);
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
    
    public Completable push() {
        return Completable.create(e -> {

            final DatabaseReference ref = ref();

            updateFirebaseUser().subscribe(new CrashReportingCompletableObserver());

            ref.updateChildren(serialize(), (firebaseError, firebase) -> {
                if (firebaseError == null) {
                    // index should be updated whenever the user is pushed
                    FirebaseEntity.pushUserMetaUpdated(model.getEntityID()).subscribe(new CrashReportingCompletableObserver());

                    ChatSDK.events().source().onNext(NetworkEvent.userMetaUpdated(model));

                    e.onComplete();
                } else {
                    e.onError(firebaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.single());
    }

    public Completable updateFirebaseUser () {
        return Completable.create(e -> {

            final FirebaseUser user = FirebaseCoreHandler.auth().getCurrentUser();

            UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                    .setDisplayName(model.getName());

            if(model.getAvatarURL() != null && model.getAvatarURL().length() > 0) {
                builder.setPhotoUri(Uri.parse(model.getAvatarURL()));
            }

            final UserProfileChangeRequest changeRequest = builder.build();

            user.updateProfile(changeRequest).addOnCompleteListener(task -> e.onComplete());
        }).subscribeOn(Schedulers.single());
    }
    
    public DatabaseReference ref(){
        return FirebasePaths.userRef(model.getEntityID());
    }

    /**
     * Set the user online value to false.
     **/
    public Completable goOffline () {

        Completable c1 = FirebaseRX.remove(FirebasePaths.userOnlineRef(model.getEntityID()));
        Completable c2 = FirebaseRX.remove(FirebasePaths.onlineRef(model.getEntityID()));

        return Completable.merge(Arrays.asList(c1, c2));
    }
    
    /**
     * Set the user online value to true.
     * 
     * When firebase disconnect this will be auto change to false.
     **/
    public Completable goOnline() {

        // Set the current state of the user as online.
        // And add a listener so when the user log off from firebase he will be set as disconnected.
        Completable c1 = FirebaseRX.set(FirebasePaths.userOnlineRef(model.getEntityID()), true, true);

        HashMap<String, Object> map = new HashMap<>();
        map.put(Keys.Time, ServerValue.TIMESTAMP);
        map.put(Keys.UID, model.getEntityID());

        Completable c2 = FirebaseRX.set(FirebasePaths.onlineRef(model.getEntityID()), map, true);

        return Completable.merge(Arrays.asList(c1, c2));
    }

    public static String processForQuery(String query){
        return StringUtils.isBlank(query) ? "" : query.replace(" ", "").toLowerCase();
    }

    public User getModel () {
        return model;
    }

}
