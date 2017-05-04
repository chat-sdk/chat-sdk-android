/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.ChatError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.Map;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.AuthManager;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;

@Deprecated
public class FirebaseAuthAdapter extends AuthManager {

    private static final String TAG = AuthManager.class.getSimpleName();
    private static boolean DEBUG = Debug.FirebaseAuthenticationHandler;

    public FirebaseAuthAdapter(Context context){
        super(context);
    }


    @Override
    @Deprecated
    public Promise<Object, ChatError, Void> authenticateWithMap(final Map<String, Object> details) {
        return null;
    }

    @Deprecated
    public Promise<BUser, ChatError, Void> handleFAUser(final FirebaseUser authData){
        return null;
    }

    @Override
    @Deprecated
    public Promise<BUser, ChatError, Void> authenticateWithCachedToken() {
        return null;
    }


    /**
     * Indicator for the current state of the authentication process.
     **/
    protected enum AuthStatus{
        IDLE {
            @Override
            public String toString() {
                return "Idle";
            }
        },
        AUTH_WITH_MAP{
            @Override
            public String toString() {
                return "Auth with map";
            }
        },
        HANDLING_F_USER{
            @Override
            public String toString() {
                return "Handling F user";
            }
        },
        UPDATING_USER{
            @Override
            public String toString() {
                return "Updating user";
            }
        },
        PUSHING_USER{
            @Override
            public String toString() {
                return "Pushing user";
            }
        },
        CHECKING_IF_AUTH{
            @Override
            public String toString() {
                return "Checking if Authenticated";
            }
        }
    }

    protected AuthStatus authingStatus = AuthStatus.IDLE;

    public AuthStatus getAuthingStatus() {
        return authingStatus;
    }

    public boolean isAuthing(){
        return authingStatus != AuthStatus.IDLE;
    }

    protected void resetAuth(){
        authingStatus = AuthStatus.IDLE;
    }



    @Override
    public Promise<Void, ChatError, Void> changePassword(String email, String oldPassword, String newPassword){
        
        final Deferred<Void, ChatError, Void> deferred = new DeferredObject<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        OnCompleteListener<Void> resultHandler = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    deferred.resolve(null);
                } else {
                    deferred.reject(getFirebaseError(DatabaseError.fromException(task.getException())));

                }
            }
        };

        user.updatePassword(newPassword).addOnCompleteListener(resultHandler);

        return deferred.promise();
    }

    @Override
    public Promise<Void, ChatError, Void> sendPasswordResetMail(String email){

        final Deferred<Void, ChatError, Void> deferred = new DeferredObject<>();

        OnCompleteListener<Void> resultHandler = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if(DEBUG) Timber.v("Email sent");
                    deferred.resolve(null);
                } else {
                    deferred.reject(getFirebaseError(DatabaseError.fromException(task.getException())));

                }
            }
        };

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(resultHandler);
        
        return deferred.promise();
    }

    @Override
    public void logout() {

        BUser user = BNetworkManager.getCoreInterface().currentUserModel();


        // Stop listening to user related alerts. (added message or thread.)
        BNetworkManager.getCoreInterface().getEventManager().userOff(user);
        
        // Removing the push channel
        if (user != null)
            BNetworkManager.getCoreInterface().getPushHandler().unsubscribeToPushChannel(user.getPushChannel());

        // Obtaining the simple login object from the ref.
        DatabaseReference ref = FirebasePaths.firebaseRef();

        // Login out
        if (user != null)
        {
            DatabaseReference userOnlineRef = FirebasePaths.userOnlineRef(user.getEntityID());
            userOnlineRef.setValue(false);
        }

        FirebaseAuth.getInstance().signOut();
    }



}
