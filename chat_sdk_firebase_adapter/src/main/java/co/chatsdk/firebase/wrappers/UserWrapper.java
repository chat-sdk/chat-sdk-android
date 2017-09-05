/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.wrappers;

import android.net.Uri;
import android.support.annotation.NonNull;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.LinkedAccount;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.AuthKeys;
import co.chatsdk.firebase.FirebasePaths;


import co.chatsdk.core.StorageManager;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.dao.DaoCore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.firebase.FirebaseReferenceManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class UserWrapper {

    private static final boolean DEBUG = Debug.User;
    
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
        User model = (User) StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, entityId);
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

//        model.setAuthenticationType((Integer) NM.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey));

        model.setEntityID(authData.getUid());


        String name = authData.getDisplayName();
        String email = authData.getEmail();
        String profileURL = authData.getPhotoUrl().toString();

//        String token = null;
//        Object tokenObject = NM.auth().getLoginInfo().get(AuthKeys.Token);
//        if(tokenObject != null) {
//            token = tokenObject.toString();
//        }
//        String uid = authData.getUid();
//
//        LinkedAccount linkedAccount;

        // Setting the name.
        if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getName())) {
            model.setName(name);
        }

        // Setting the email.//
        if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getEmail())) {
            model.setEmail(email);
        }

        if (StringUtils.isNotBlank(profileURL) && StringUtils.isBlank(model.getAvatarURL())) {
            model.setAvatarURL(profileURL);
            model.setThumbnailURL(profileURL);
        }


//        switch ((Integer) (NM.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey)))
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

        if (StringUtils.isEmpty(model.getName()))
        {
            model.setName(Defines.getDefaultUserName());
        }
        if(StringUtils.isEmpty(model.getAvatarURL())) {
            String url = Defines.getDefaultImageUrl("http://robohash.org/" + name,
                    Defines.ImageProperties.INITIALS_IMAGE_SIZE,
                    Defines.ImageProperties.INITIALS_IMAGE_SIZE);
            model.setAvatarURL(url);
            model.setThumbnailURL(url);
        }
        
        // Save the bundle
        DaoCore.updateEntity(model);
    }

    public Completable once(){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final DatabaseReference ref = ref();
                if (DEBUG) Timber.v("once, EntityID: %s, Ref Path: %s", model.getEntityID(), ref.toString());

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        deserialize((Map<String, Object>) snapshot.getValue());
                        e.onComplete();
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
//                        e.onError(firebaseError.toException());
                    }
                });

            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<User> metaOn() {
        return Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(final ObservableEmitter<User> e) throws Exception {

                final DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());

                if(FirebaseReferenceManager.shared().isOn(userMetaRef)) {
                    e.onNext(model);
                }

                ValueEventListener listener = userMetaRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            deserializeMeta((Map<String, Object>) snapshot.getValue());
                            e.onNext(model);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        e.onError(databaseError.toException());
                    }
                });
                FirebaseReferenceManager.shared().addRef(userMetaRef, listener);
            }
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }


    public void metaOff(){
        DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListener(userMetaRef);
    }


    void deserialize(Map<String, Object> value){
        if (DEBUG) Timber.v("deserialize, Value is null? %s", value == null);
        
        if (value != null)
        {
            if (value.containsKey(Keys.Online) && !value.get(Keys.Online).equals(""))
                model.setOnline((Boolean) value.get(Keys.Online));

            if (value.containsKey(Keys.Color) && !value.get(Keys.Color).equals("")) {
                model.setMessageColor((String) value.get(Keys.Color));
            }

            // The entity update is called in the deserializeMeta.
            deserializeMeta((Map<String, Object>) value.get(FirebasePaths.MetaPath));
        }
    }

    void deserializeMeta(Map<String, Object> value){
        if (DEBUG) Timber.v("deserializeMeta, Value: %s", value);
        
        if (value != null)
        {
            Map<String, Object> oldData = model.metaMap();
            Map<String, Object> newData = value;

            if (DEBUG) Timber.v("deserializeMeta, OldDataMap: %s", oldData);
            
            // Updating the old bundle
            for (String key : newData.keySet())
            {
                if (DEBUG) Timber.d("key: %s, Value: %s", key, newData.get(key));
                
                if (oldData.get(key) == null || !oldData.get(key).equals(newData.get(key)))
                    oldData.put(key, newData.get(key));
            }

            model.setMetaMap(oldData);

            model = DaoCore.updateEntity(model);
        }
    }

    Map<String, Object> serialize(){
        Map<String, Object> values = new HashMap<String, Object>();

        values.put(Keys.Color, StringUtils.isEmpty(model.getMessageColor()) ? "" : model.getMessageColor());
        values.put(Keys.Meta, model.metaMap());
        values.put(Keys.LastOnline, ServerValue.TIMESTAMP);

        return values;
    }
    
    public Completable push(){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final DatabaseReference ref = ref();

                updateFirebaseUser();

                ref.updateChildren(serialize(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError == null) {
                            // index should be updated whenever the user is pushed
                            e.onComplete();
                        }
                        else {
                            e.onError(firebaseError.toException());
                        }
                    }
                });
            }
        }).andThen(updateIndex());
    }

    public Completable updateFirebaseUser () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                        .setDisplayName(model.getName());

                if(model.getAvatarURL() != null && model.getAvatarURL().length() > 0) {
                    builder.setPhotoUri(Uri.parse(model.getAvatarURL()));
                }

                final UserProfileChangeRequest changeRequest = builder.build();

                user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        e.onComplete();
                    }
                });
            }
        });
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
    
    public String pushChannel(){
        String channel = USER_PREFIX + (model.getEntityID().replace(":", "_"));
        
        if (channel.contains("%3A"))
            channel = channel.replace("%3A", "_");
        if (channel.contains("%253A"))
            channel = channel.replace("%253A", "_");
        
        return channel;
    }

    public Completable updateIndex(){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final Map<String, String> values = new HashMap<String, String>();

                String name = model.getName();
                String email = model.getEmail();
                String phoneNumber = model.metaStringForKey(Keys.Phone);

                values.put(Keys.Name, StringUtils.isNotEmpty(name) ? processForQuery(name) : "");
                values.put(Keys.Email, StringUtils.isNotEmpty(email) ? processForQuery(email) : "");

                if (Defines.IndexUserPhoneNumber && StringUtils.isNotBlank(phoneNumber)) {
                    values.put(Keys.Phone, processForQuery(phoneNumber));
                }

                final DatabaseReference ref = FirebasePaths.indexRef().child(model.getEntityID());


                ref.setValue(values, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError == null) {
                            e.onComplete();
                        } else {
                            e.onError(firebaseError.toException());
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Set the user online value to false.
     **/
    public void goOffline(){
        DatabaseReference userOnlineRef = FirebasePaths.userOnlineRef(model.getEntityID());
        userOnlineRef.setValue(false);
    }
    
    /**
     * Set the user online value to true.
     * 
     * When firebase disconnect this will be auto change to false.
     **/
    public void goOnline(){
        DatabaseReference userOnlineRef = FirebasePaths.userOnlineRef(model.getEntityID());

        // Set the current state of the user as online.
        // And add a listener so when the user log off from firebase he will be set as disconnected.
        userOnlineRef.setValue(true);
        userOnlineRef.onDisconnect().setValue(false);
    }

    public static String processForQuery(String query){
        return StringUtils.isBlank(query) ? "" : query.replace(" ", "").toLowerCase();
    }

    public User getModel () {
        return model;
    }

}
