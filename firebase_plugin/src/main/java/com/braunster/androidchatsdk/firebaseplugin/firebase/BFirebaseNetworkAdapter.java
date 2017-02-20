/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:34 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.braunster.androidchatsdk.firebaseplugin.R;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.BBackendlessHandler;
import com.braunster.androidchatsdk.firebaseplugin.firebase.backendless.ChatSDKReceiver;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BFirebaseDefines;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Anonymous;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Custom;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Password;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Register;
import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Keys;

public abstract class BFirebaseNetworkAdapter extends AbstractNetworkAdapter {

    private static final String TAG = BFirebaseNetworkAdapter.class.getSimpleName();
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    public BFirebaseNetworkAdapter(Context context){
        super(context);

        // Adding the manager that will handle all the incoming events.
        FirebaseEventsManager eventManager = FirebaseEventsManager.getInstance();
        setEventManager(eventManager);

        // Setting the upload handler
        setUploadHandler(new BFirebaseUploadHandler());

        // Setting the push handler
        BBackendlessHandler backendlessPushHandler = new BBackendlessHandler();
        backendlessPushHandler.setContext(context);
        setPushHandler(backendlessPushHandler);

        // Parse init
        /*Parse.initialize(context, context.getString(R.string.parse_app_id), context.getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();*/

        backendlessPushHandler.initWithAppKey(context.getString(R.string.backendless_app_id),
                            context.getString(R.string.backendless_secret_key), context.getString(R.string.backendless_app_version));
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
    public Promise<Object, BError, Void> authenticateWithMap(final Map<String, Object> details) {
        if (DEBUG) Timber.v("authenticateWithMap, KeyType: %s", details.get(BDefines.Prefs.LoginTypeKey));

        final Deferred<Object, BError, Void> deferred = new DeferredObject<>();

        if (isAuthing())
        {
            if (DEBUG) Timber.d("Already Authing!, Status: %s", authingStatus.name());
            deferred.reject(BError.getError(BError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
            return deferred.promise();
        }

        authingStatus = AuthStatus.AUTH_WITH_MAP;

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

                AbstractNetworkAdapter.provider = BDefines.ProviderString.Facebook;
                AbstractNetworkAdapter.token = BFacebookManager.userFacebookAccessToken;

                credential = FacebookAuthProvider.getCredential(BFacebookManager.userFacebookAccessToken);
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                break;

            case Twitter:

                if (DEBUG) Timber.d("authing with twitter, AccessToken: %s", TwitterManager.accessToken.getToken());

                AbstractNetworkAdapter.provider = BDefines.ProviderString.Twitter;
                AbstractNetworkAdapter.token = TwitterManager.accessToken.getToken();

                credential = TwitterAuthProvider.getCredential(TwitterManager.accessToken.getToken(), TwitterManager.accessToken.getSecret());
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                break;

            case Password:

                AbstractNetworkAdapter.provider = BDefines.ProviderString.Password;

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

                AbstractNetworkAdapter.provider = BDefines.ProviderString.Anonymous;

                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(resultHandler);
                break;

            case Custom:

                AbstractNetworkAdapter.provider = BDefines.ProviderString.Custom;

                FirebaseAuth.getInstance().signInWithCustomToken((String) details.get(BDefines.Prefs.TokenKey)).addOnCompleteListener(resultHandler);

                break;


            default:
                if (DEBUG) Timber.d("No login type was found");
                deferred.reject(BError.getError(BError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                break;
        }


        return deferred.promise();
    }

    public abstract Promise<BUser, BError, Void> handleFAUser(final FirebaseUser authData);


    @Override
    public String getServerURL() {
        return BDefines.ServerUrl;
    }

    
    
    protected void pushForMessage(final BMessage message){
        if (!backendlessEnabled())
            return;

        if (DEBUG) Timber.v("pushForMessage");
        if (message.getThread().getTypeSafely() == BThread.Type.Private) {

            // Loading the message from firebase to get the timestamp from server.
            DatabaseReference firebase = FirebasePaths.threadRef(message.getThread().getEntityID())
                    .child(BFirebaseDefines.Path.BMessagesPath)
                    .child(message.getEntityID());

            firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Long date = null;
                    try {
                        date = (Long) snapshot.child(Keys.BDate).getValue();
                    } catch (ClassCastException e) {
                        date = (((Double)snapshot.child(Keys.BDate).getValue()).longValue());
                    }
                    finally {
                        if (date != null)
                        {
                            message.setDate(new Date(date));
                            DaoCore.updateEntity(message);
                        }
                    }

                    // If we failed to get date dont push.
                    if (message.getDate()==null)
                        return;

                    BUser currentUser = currentUserModel();
                    List<BUser> users = new ArrayList<BUser>();

                    for (BUser user : message.getThread().getUsers())
                        if (!user.equals(currentUser))
                            if (!user.equals(currentUser)) {
                                // Timber.v(user.getEntityID() + ", " + user.getOnline().toString());
                                // sends push notification regardless of receiver online status
                                // TODO: add observer to online status
                                // if (user.getOnline() == null || !user.getOnline())
                                users.add(user);
                            }

                    pushToUsers(message, users);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
    }

    protected void pushToUsers(BMessage message, List<BUser> users){
        if (DEBUG) Timber.v("pushToUsers");

        if (!backendlessEnabled() || users.size() == 0)
            return;

        // We're identifying each user using push channels. This means that
        // when a user signs up, they register with backendless on a particular
        // channel. In this case user_[user id] this means that we can
        // send a push to a specific user if we know their user id.
        List<String> channels = new ArrayList<String>();
        for (BUser user : users)
            channels.add(user.getPushChannel());

        if (DEBUG) Timber.v("pushutils sendmessage");
        String messageText = message.getText();

        if (message.getType() == LOCATION)
            messageText = "Location Message";
        else if (message.getType() == IMAGE)
            messageText = "Picture Message";

        String sender = message.getBUserSender().getMetaName();
        String fullText = sender + " " + messageText;

        JSONObject data = new JSONObject();
        try {
            data.put(BDefines.Keys.ACTION, ChatSDKReceiver.ACTION_MESSAGE);

            data.put(BDefines.Keys.CONTENT, fullText);
            data.put(BDefines.Keys.MESSAGE_ENTITY_ID, message.getEntityID());
            data.put(BDefines.Keys.THREAD_ENTITY_ID, message.getThread().getEntityID());
            data.put(BDefines.Keys.MESSAGE_DATE, message.getDate().getTime());
            data.put(BDefines.Keys.MESSAGE_SENDER_ENTITY_ID, message.getBUserSender().getEntityID());
            data.put(BDefines.Keys.MESSAGE_SENDER_NAME, message.getBUserSender().getMetaName());
            data.put(BDefines.Keys.MESSAGE_TYPE, message.getType());
            data.put(BDefines.Keys.MESSAGE_PAYLOAD, message.getText());
            //For iOS
            data.put(BDefines.Keys.BADGE, BDefines.Keys.INCREMENT);
            data.put(BDefines.Keys.ALERT, fullText);
            // For making sound in iOS
            data.put(BDefines.Keys.SOUND, BDefines.Keys.Default);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pushHandler.pushToChannels(channels, data);
    }


    /** Convert the firebase error to a {@link com.braunster.chatsdk.object.BError BError} object. */
    public static BError getFirebaseError(DatabaseError error){
        String errorMessage = "";

        int code = 0;

        switch (error.getCode())
        {
            /*case DatabaseError.EMAIL_TAKEN:
                code = BError.Code.EMAIL_TAKEN;
                errorMessage = "Email is taken.";
                break;

            case DatabaseError.INVALID_EMAIL:
                code = BError.Code.INVALID_EMAIL;
                errorMessage = "Invalid Email.";
                break;

            case DatabaseError.INVALID_PASSWORD:
                code = BError.Code.INVALID_PASSWORD;
                errorMessage = "Invalid Password";
                break;

            case DatabaseError.USER_DOES_NOT_EXIST:
                code = BError.Code.USER_DOES_NOT_EXIST;
                errorMessage = "Account not found.";
                break;

            case DatabaseError.INVALID_CREDENTIALS:
                code = BError.Code.INVALID_CREDENTIALS;
                errorMessage = "Invalid credentials.";
                break;*/

            case DatabaseError.NETWORK_ERROR:
                code = BError.Code.NETWORK_ERROR;
                errorMessage = "Network Error.";
                break;

            case DatabaseError.EXPIRED_TOKEN:
                code = BError.Code.EXPIRED_TOKEN;
                errorMessage = "Expired Token.";
                break;

            case DatabaseError.OPERATION_FAILED:
                code = BError.Code.OPERATION_FAILED;
                errorMessage = "Operation failed";
                break;

            case DatabaseError.PERMISSION_DENIED:
                code = BError.Code.PERMISSION_DENIED;
                errorMessage = "Permission denied";
                break;

            case DatabaseError.DISCONNECTED:
                code = BError.Code.DISCONNECTED;
                errorMessage = "Disconnected.";
                break;

            case DatabaseError.INVALID_TOKEN:
                code = BError.Code.INVALID_TOKEN;
                errorMessage = "Invalid token.";
                break;

            case DatabaseError.MAX_RETRIES:
                code = BError.Code.MAX_RETRIES;
                errorMessage = "Max retries.";
                break;

            case DatabaseError.OVERRIDDEN_BY_SET:
                code = BError.Code.OVERRIDDEN_BY_SET;
                errorMessage = "Overridden by set.";
                break;

            case DatabaseError.UNAVAILABLE:
                code = BError.Code.UNAVAILABLE;
                errorMessage = "Unavailable.";
                break;

            case DatabaseError.UNKNOWN_ERROR:
                code = BError.Code.UNKNOWN_ERROR;
                errorMessage = "Unknown error.";
                break;

            case DatabaseError.USER_CODE_EXCEPTION:
                code = BError.Code.USER_CODE_EXCEPTION;

                String[] stacktrace = error.toException().getMessage().split(": ");

                String[] message = stacktrace[2].split("\\.");

                errorMessage = message[0];
                break;

            case DatabaseError.WRITE_CANCELED:
                code = BError.Code.WRITE_CANCELED;
                errorMessage = "Write canceled.";
                break;

            default: errorMessage = "An Error Occurred.";
        }

        return new BError(code, errorMessage, error);
    }
}
