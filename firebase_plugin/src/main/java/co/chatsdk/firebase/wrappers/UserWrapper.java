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
import co.chatsdk.firebase.FirebasePaths;


import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BLinkedAccount;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.dao.DaoCore;

import com.braunster.chatsdk.network.TwitterManager;
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
import timber.log.Timber;


public class UserWrapper {

    private static final boolean DEBUG = Debug.BUser;
    
    private static final String USER_PREFIX = "user";
    private BUser model;

    public static UserWrapper initWithAuthData(FirebaseUser authData){
        return new UserWrapper(authData);
    }

    public static UserWrapper initWithModel(BUser user){
        return new UserWrapper(user);
    }
    
    public static UserWrapper initWithSnapshot(DataSnapshot snapshot){
        return new UserWrapper(snapshot);
    }

    public static UserWrapper initWithEntityId(String entityId){
        BUser model = (BUser) StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, entityId);
        return initWithModel(model);
    }
    
    private UserWrapper(FirebaseUser authData){
        model = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, authData.getUid());
        updateUserFromAuthData(authData);
    }

    public UserWrapper(BUser model) {
        this.model = model;
    }
    
    public UserWrapper(DataSnapshot snapshot){
        model = StorageManager.shared().fetchOrCreateEntityWithEntityID(BUser.class, snapshot.getKey());
        deserialize((Map<String, Object>) snapshot.getValue());
    }
    
    /**
     * Note - Change was removing of online values as set online and online time.
     * * * * */
    private void updateUserFromAuthData(FirebaseUser authData){
        Timber.v("updateUserFromAuthData");

        model.setAuthenticationType((Integer) NM.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey));

        model.setEntityID(authData.getUid());


        String name = authData.getDisplayName();
        String email = authData.getEmail();
        String token = null;
        Object tokenObject = NM.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.TokenKey);
        if(tokenObject != null) {
            token = tokenObject.toString();
        }
        String uid = authData.getUid();

        BLinkedAccount linkedAccount;
        
        switch ((Integer) (NM.auth().getLoginInfo().get(co.chatsdk.core.types.Defines.Prefs.AccountTypeKey)))
        {
            case Defines.ProviderInt.Facebook:
                // Setting the name.
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                {
                    model.setMetaName(name);
                }

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
                }

                linkedAccount = model.getAccountWithType(BLinkedAccount.Type.FACEBOOK);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.FACEBOOK);
                    linkedAccount.setUserId(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken(token);

                break;

            case Defines.ProviderInt.Twitter:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                    model.setMetaName(name);

                // Setting the email.//
                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
                }

                TwitterManager.userId = uid;

                linkedAccount = model.getAccountWithType(BLinkedAccount.Type.TWITTER);
                if (linkedAccount == null)
                {
                    linkedAccount = new BLinkedAccount();
                    linkedAccount.setType(BLinkedAccount.Type.TWITTER);
                    linkedAccount.setUserId(model.getId());
                    DaoCore.createEntity(linkedAccount);
                }
                linkedAccount.setToken(token);

                break;

            case Defines.ProviderInt.Password:
                // Setting the name
                if (StringUtils.isNotBlank(name) && StringUtils.isBlank(model.getMetaName()))
                    model.setMetaName(name);

                if (StringUtils.isNotBlank(email) && StringUtils.isBlank(model.getMetaEmail()))
                {
                    model.setMetaEmail(email);
                }
                break;

            default: break;
        }

        // CoreMessage Color.
        if (StringUtils.isEmpty(model.getMessageColor()))
        {
            if (StringUtils.isNotEmpty(DaoDefines.Defaults.MessageColor))
            {
                model.setMessageColor(DaoDefines.Defaults.MessageColor);
            }
        }

        if (StringUtils.isEmpty(model.getMetaName()))
        {
            model.setMetaName(Defines.getDefaultUserName());
        }
        
        // Save the data
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
                        e.onError(firebaseError.toException());
                    }
                });

            }
        });
    }

    public Observable<BUser> metaOn() {
        return Observable.create(new ObservableOnSubscribe<BUser>() {
            @Override
            public void subscribe(final ObservableEmitter<BUser> e) throws Exception {

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
                        e.onError(databaseError.toException());
                    }
                });
                FirebaseReferenceManager.shared().addRef(userMetaRef, listener);
            }
        });
    }


    public void metaOff(){
        DatabaseReference userMetaRef = FirebasePaths.userMetaRef(model.getEntityID());
        FirebaseReferenceManager.shared().removeListener(userMetaRef);
    }


    void deserialize(Map<String, Object> value){
        if (DEBUG) Timber.v("deserialize, Value is null? %s", value == null);
        
        if (value != null)
        {
            if (value.containsKey(DaoDefines.Keys.Online) && !value.get(DaoDefines.Keys.Online).equals(""))
                model.setOnline((Boolean) value.get(DaoDefines.Keys.Online));

            if (value.containsKey(DaoDefines.Keys.Color) && !value.get(DaoDefines.Keys.Color).equals("")) {
                model.setMessageColor((String) value.get(DaoDefines.Keys.Color));
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
            
            // Updating the old data
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

        values.put(DaoDefines.Keys.Color, StringUtils.isEmpty(model.getMessageColor()) ? "" : model.getMessageColor());
        values.put(DaoDefines.Keys.Meta, model.metaMap());
        values.put(DaoDefines.Keys.LastOnline, ServerValue.TIMESTAMP);

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
                        .setDisplayName(model.getMetaName());

                if(model.getMetaPictureUrl() != null && model.getMetaPictureUrl().length() > 0) {
                    builder.setPhotoUri(Uri.parse(model.getMetaPictureUrl()));
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
    
//    public Completable addThreadWithEntityID(final String entityID){
//        return Completable.create(new CompletableOnSubscribe() {
//            @Override
//            public void subscribe(final CompletableEmitter e) throws Exception {
//
//                DatabaseReference userThreadsRef = FirebasePaths.userThreadsRef(model.getEntityID()).child(entityID);
//
//                HashMap<String, Object> value = new HashMap<>();
//                value.put(DaoDefines.Keys.Null, "");
//
//                userThreadsRef.setValue(value, new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
//                        if (firebaseError == null) {
//                            e.onComplete();
//                        }
//                        else {
//                            e.onError(firebaseError.toException());
//                        }
//                    }
//                });
//
//            }
//        });
//    }
//
//    public Completable removeThreadWithEntityID(final String entityID){
//        return Completable.create(new CompletableOnSubscribe() {
//            @Override
//            public void subscribe(final CompletableEmitter e) throws Exception {
//
//                DatabaseReference userThreadRef = FirebasePaths.userThreadsRef(model.getEntityID()).child(entityID);
//
//                userThreadRef.removeValue(new DatabaseReference.CompletionListener() {
//                    @Override
//                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
//                        if (firebaseError != null) {
//                            e.onError(firebaseError.toException());
//                        }
//                        else {
//                            e.onComplete();
//                        }
//                    }
//                });
//
//            }
//        });
//    }
    
    public Completable updateIndex(){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final Map<String, String> values = new HashMap<String, String>();

                String name = model.getMetaName();
                String email = model.getMetaEmail();
                String phoneNumber = model.metaStringForKey(DaoDefines.Keys.Phone);

                values.put(DaoDefines.Keys.Name, StringUtils.isNotEmpty(name) ? processForQuery(name) : "");
                values.put(DaoDefines.Keys.Email, StringUtils.isNotEmpty(email) ? processForQuery(email) : "");

                if (Defines.IndexUserPhoneNumber && StringUtils.isNotBlank(phoneNumber)) {
                    values.put(DaoDefines.Keys.Phone, processForQuery(phoneNumber));
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

    public BUser getModel () {
        return model;
    }

}
