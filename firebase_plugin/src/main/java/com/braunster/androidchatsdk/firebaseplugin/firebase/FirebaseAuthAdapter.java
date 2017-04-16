/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.object.BError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import tk.wanderingdevelopment.chatsdk.core.abstracthandlers.AuthManager;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Anonymous;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Custom;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Password;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Register;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Prefs;

public class FirebaseAuthAdapter extends AuthManager {

    private static final String TAG = AuthManager.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    public FirebaseAuthAdapter(Context context){
        super(context);
    }


    @Override
    public Promise<Object, BError, Void> authenticateWithMap(final Map<String, Object> details) {
        if (DEBUG) Timber.v("authenticateWithMap, KeyType: %s", details.get(BDefines.Prefs.LoginTypeKey));

        final Deferred<Object, BError, Void> deferred = new DeferredObject<>();

        if (isAuthing())
        {
            if (DEBUG) Timber.d("Already Authing!, Status: %s", authingStatus.name());
            deferred.reject(BError.getError(BError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
            return deferred.promise();
        }

        authingStatus = FirebaseAuthAdapter.AuthStatus.AUTH_WITH_MAP;

        OnCompleteListener<AuthResult> resultHandler = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull final Task<AuthResult> task) {
                if(task.isComplete() && task.isSuccessful()) {
                    handleFAUser(task.getResult().getUser()).then(new DoneCallback<BUser>() {
                        @Override
                        public void onDone(BUser bUser) {
                            resetAuth();
                            deferred.resolve(task.getResult().getUser());
                            resetAuth();
                        }
                    }, new FailCallback<BError>() {
                        @Override
                        public void onFail(BError bError) {
                            resetAuth();
                            deferred.reject(bError);
                        }
                    });
                } else {
                    if (DEBUG) Timber.e("Error login in, Name: %s", task.getException().getMessage());
                    resetAuth();
                    deferred.reject(BError.getExceptionError(task.getException()));
                }
            }
        };

        AuthCredential credential = null;

        switch ((Integer)details.get(BDefines.Prefs.LoginTypeKey))
        {
            case Facebook:

                if (DEBUG) Timber.d(TAG, "authing with fb, AccessToken: %s", BFacebookManager.userFacebookAccessToken);

                AuthManager.provider = BDefines.ProviderString.Facebook;
                AuthManager.token = BFacebookManager.userFacebookAccessToken;

                credential = FacebookAuthProvider.getCredential(BFacebookManager.userFacebookAccessToken);
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                break;

            case Twitter:

                if (DEBUG) Timber.d("authing with twitter, AccessToken: %s", TwitterManager.accessToken.getToken());

                AuthManager.provider = BDefines.ProviderString.Twitter;
                AuthManager.token = TwitterManager.accessToken.getToken();

                credential = TwitterAuthProvider.getCredential(TwitterManager.accessToken.getToken(), TwitterManager.accessToken.getSecret());
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                break;

            case Password:

                AuthManager.provider = BDefines.ProviderString.Password;

                FirebaseAuth.getInstance().signInWithEmailAndPassword((String) details.get(BDefines.Prefs.LoginEmailKey),
                        (String) details.get(BDefines.Prefs.LoginPasswordKey)).addOnCompleteListener(resultHandler);
                break;
            case  Register:
                FirebaseAuth.getInstance().createUserWithEmailAndPassword((String) details.get(BDefines.Prefs.LoginEmailKey),
                        (String) details.get(BDefines.Prefs.LoginPasswordKey)).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // Resetting so we could auth again.
                        resetAuth();

                        if(task.isSuccessful()) {
                            //Authing the user after creating it.
                            details.put(BDefines.Prefs.LoginTypeKey, Password);
                            authenticateWithMap(details).done(new DoneCallback<Object>() {
                                @Override
                                public void onDone(Object o) {
                                    deferred.resolve(o);
                                }
                            }).fail(new FailCallback<BError>() {
                                @Override
                                public void onFail(BError bError) {
                                    deferred.reject(bError);
                                }
                            });
                        } else {
                            if (DEBUG) Timber.e("Error login in, Name: %s", task.getException().getMessage());
                            resetAuth();
                            deferred.reject(getFirebaseError(DatabaseError.fromException(task.getException())));
                        }
                    }
                });
                break;

            case Anonymous:

                AuthManager.provider = BDefines.ProviderString.Anonymous;

                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(resultHandler);
                break;

            case Custom:

                AuthManager.provider = BDefines.ProviderString.Custom;

                FirebaseAuth.getInstance().signInWithCustomToken((String) details.get(BDefines.Prefs.TokenKey)).addOnCompleteListener(resultHandler);

                break;


            default:
                if (DEBUG) Timber.d("No login type was found");
                deferred.reject(BError.getError(BError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                break;
        }


        return deferred.promise();
    }

    public Promise<BUser, BError, Void> handleFAUser(final FirebaseUser authData){
        if (DEBUG) Timber.v("handleFAUser");

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();
        
        authingStatus = AuthStatus.HANDLING_F_USER;

        if (authData == null)
        {
            resetAuth();
            // If the user isn't authenticated they'll need to login
            deferred.reject(new BError(BError.Code.SESSION_CLOSED));
        }
        else
        {
            // Flag that the user has been authenticated
            setAuthenticated(true);

            // Save the authentication ID for the current user
            // Set the current user
            final Map<String, Object> loginInfoMap = new HashMap<String, Object>();

            String aid = authData.getUid();
            Timber.v("Uid: " + aid);
            loginInfoMap.put(Prefs.AuthenticationID, aid);

            String provider = AuthManager.provider;
            if(provider.equals("")) {
                provider = getLoginInfo().get(Prefs.AccountTypeKey).toString();
                loginInfoMap.put(Prefs.AccountTypeKey, (Integer.getInteger(provider)));
            } else {
                loginInfoMap.put(Prefs.AccountTypeKey, FirebasePaths.providerToInt(provider));
            }
            Timber.v("Provider: " + provider);

            String token = AuthManager.token;
            if(getLoginInfo().get(Prefs.TokenKey) != null && token.equals("")) token = getLoginInfo().get(Prefs.TokenKey).toString();
            Timber.v("Token: " + token);
            loginInfoMap.put(Prefs.TokenKey, token);

            setLoginInfo(loginInfoMap);
            resetAuth();

            // Doint a once() on the user to push its details to firebase.
            final BUserWrapper wrapper = BUserWrapper.initWithAuthData(authData);
            wrapper.once().then(new DoneCallback<BUser>() {
                @Override
                public void onDone(BUser bUser) {
                    
                    if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");
                    DaoCore.updateEntity(bUser);

                    BNetworkManager.getCoreInterface().getEventManager().userOn(bUser);
                    
                    // TODO push a default image of the user to the cloud.

                    if(!BNetworkManager.getCoreInterface().getPushHandler().subscribeToPushChannel(wrapper.pushChannel())) {
                        deferred.reject(new BError(BError.Code.BACKENDLESS_EXCEPTION));
                    }

                    BNetworkManager.getCoreInterface().goOnline();

                    wrapper.push().done(new DoneCallback<BUser>() {
                        @Override
                        public void onDone(BUser u) {

                            if (DEBUG) Timber.v("OnDone, user was pushed from firebase.");
                            resetAuth();
                            deferred.resolve(u);
                        }
                    }).fail(new FailCallback<BError>() {
                        @Override
                        public void onFail(BError error) {
                            resetAuth();
                            deferred.reject(error);
                        }
                    });
                }
            }, new FailCallback<BError>() {
                @Override
                public void onFail(BError bError) {
                    deferred.reject(bError);
                }
            });
        }

        return deferred.promise();

    }

    @Override
    public Promise<BUser, BError, Void> checkUserAuthenticated() {
        if (DEBUG) Timber.v("checkUserAuthenticatedWithCallback, %s", getLoginInfo().get(Prefs.AccountTypeKey));

        final Deferred<BUser, BError, Void> deferred = new DeferredObject<>();

        if (isAuthing())
        {
            if (DEBUG) Timber.d("Already Authing!, Status: %s", authingStatus.name());

            deferred.reject(BError.getError(BError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
        }
        else
        {
            authingStatus = AuthStatus.CHECKING_IF_AUTH;

            if (!getLoginInfo().containsKey(Prefs.AccountTypeKey))
            {
                if (DEBUG) Timber.d(TAG, "No account type key");

                resetAuth();
                deferred.reject(new BError(BError.Code.NO_LOGIN_INFO));
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user!=null)
            {
                handleFAUser(user).done(new DoneCallback<BUser>() {
                    @Override
                    public void onDone(BUser bUser) {
                        resetAuth();
                        deferred.resolve(bUser);
                    }
                }).fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError bError) {
                        resetAuth();
                        deferred.reject(bError);
                    }
                });
            }
            else{
                resetAuth();
                deferred.reject(BError.getError(BError.Code.NO_AUTH_DATA, "No auth data found"));
            }
        }

        return deferred.promise();
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
    public Promise<Void, BError, Void> changePassword(String email, String oldPassword, String newPassword){
        
        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

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
    public Promise<Void, BError, Void> sendPasswordResetMail(String email){

        final Deferred<Void, BError, Void> deferred = new DeferredObject<>();

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

        /* No need to logout from facebook due to the fact that the logout from facebook event will trigger this event.
        *  The logout from fb is taking care of by the fb login button.*/
        setAuthenticated(false);

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
