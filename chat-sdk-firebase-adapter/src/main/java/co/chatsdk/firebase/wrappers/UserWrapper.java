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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.android.schedulers.AndroidSchedulers;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.StringChecker;
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

    private static final String USER_PREFIX = "user";
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
        User model = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, entityId);
        return initWithModel(model);
    }
    
    private UserWrapper(FirebaseUser authData){
        model = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, authData.getUid());
        updateUserFromAuthData(authData);
    }

    public UserWrapper(User model) {
        this.model = model;
    }
    
    public UserWrapper(DataSnapshot snapshot){
        model = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, snapshot.getKey());
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

        // Setting the email.//
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

//        switch ((Integer) (ChatSDK.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey)))
//        {
//            case Defines.ProviderInt.Facebook:
//                linkedAccount = model.getAccountWithType(LinkedAccount.Type.FACEBOOK);
//                if (linkedAccount == null)
//                {
//                    linkedAccount = new LinkedAccount();
//                    linkedAccount.setType(LinkedAccount.Type.FACEBOOK);
//                    linkedAccount.setUserId(model.getId());
//                    DaoCore.createEntity(linkedAccount);
//                }
//                linkedAccount.setToken(token);
//
//                break;
//
//            case Defines.ProviderInt.Twitter:
//                TwitterManager.userId = uid;
//
//                linkedAccount = model.getAccountWithType(LinkedAccount.Type.TWITTER);
//                if (linkedAccount == null)
//                {
//                    linkedAccount = new LinkedAccount();
//                    linkedAccount.setType(LinkedAccount.Type.TWITTER);
//                    linkedAccount.setUserId(model.getId());
//                    DaoCore.createEntity(linkedAccount);
//                }
//                linkedAccount.setToken(token);
//
//                break;
//
//            case Defines.ProviderInt.Password:
//                break;
//
//            default: break;
//        }

    }

    public Completable once(){
        return Completable.create(e -> {

            final DatabaseReference ref = ref();

            ref.addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                if(hasValue) {
                    deserialize((Map<String, Object>) snapshot.getValue());
                }
                e.onComplete();
            }));

        }).subscribeOn(AndroidSchedulers.mainThread());
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
            Map<String, Object> newData = value;

            // Updating the old bundle
            for (String key : newData.keySet()) {
                if (oldData.get(key) == null || !oldData.get(key).equals(newData.get(key))) {
                    // We don't store availability data in the Firebase meta - it's handled by the online flag
                    if (!key.equals(Keys.Availability)) {
                        oldData.put(key, newData.get(key).toString());
                    }
                }
            }

            model.setMetaMap(oldData);

            model = DaoCore.updateEntity(model);
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

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<>();

        // Don't push availability to Firebase
        HashMap<String, String> metaMap = new HashMap<>(model.metaMap());
        metaMap.remove(Keys.Availability);
        metaMap.put(Keys.NameLowercase, model.getName() != null ? model.getName().toLowerCase() : "");

        values.put(Keys.Meta, metaMap);
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
                    e.onComplete();
                }
                else {
                    e.onError(firebaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.single()).andThen(updateIndex());
    }

    public Completable updateFirebaseUser () {
        return Completable.create(e -> {

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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

    private DatabaseReference imageRef(){
        return ref().child(FirebasePaths.Image);
    }

    private DatabaseReference thumbnailRef(){
        return ref().child(FirebasePaths.Thumbnail);
    }

    private DatabaseReference metaRef(){
        return ref().child(FirebasePaths.MetaPath);
    }

//    public String pushChannel() {
//        return model.getEntityID();
//        String channel = USER_PREFIX + (model.getEntityID().replace(":", "_"));
//
//        if (channel.contains("%3A"))
//            channel = channel.replace("%3A", "_");
//        if (channel.contains("%253A"))
//            channel = channel.replace("%253A", "_");
//
//        return channel;
//    }

    public Completable updateIndex() {
        return Completable.create(e -> {

            final Map<String, String> values = new HashMap<String, String>();

            String name = model.getName();
            String email = model.getEmail();
            String phoneNumber = model.metaStringForKey(Keys.Phone);

            values.put(Keys.Name, StringUtils.isNotEmpty(name) ? processForQuery(name) : "");
            values.put(Keys.Email, StringUtils.isNotEmpty(email) ? processForQuery(email) : "");
            values.put(Keys.Phone, StringUtils.isNotEmpty(phoneNumber) ? processForQuery(phoneNumber) : "");

            final DatabaseReference ref = FirebasePaths.indexRef().child(model.getEntityID());


            ref.setValue(values, (firebaseError, firebase) -> {
                if (firebaseError == null) {
                    e.onComplete();
                } else {
                    e.onError(firebaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.single());
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
