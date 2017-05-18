package co.chatsdk.firebase;

import android.support.annotation.NonNull;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.object.ChatError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.enums.AuthStatus;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.LoginType;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseAuthenticationHandler extends AbstractAuthenticationHandler {

    private static final String TAG = FirebaseAuthenticationHandler.class.getSimpleName();
    private static boolean DEBUG = Debug.FirebaseAuthenticationHandler;


    public Completable authenticateWithCachedToken() {
        return Single.create(new SingleOnSubscribe<FirebaseUser>() {
            @Override
            public void subscribe(final SingleEmitter<FirebaseUser> e) throws Exception {
                if (DEBUG) Timber.v("checkUserAuthenticatedWithCallback, %s", getLoginInfo().get(Defines.Prefs.AccountTypeKey));

                final Deferred<BUser, ChatError, Void> deferred = new DeferredObject<>();

                if (isAuthenticating())
                {
                    if (DEBUG) Timber.d("Already Authing!, Status: %s", getAuthStatus().name());
                    e.onError(ChatError.getError(ChatError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
                }
                else
                {
                    setAuthStatus(AuthStatus.CHECKING_IF_AUTH);

                    if (!getLoginInfo().containsKey(Defines.Prefs.AccountTypeKey))
                    {
                        if (DEBUG) Timber.d(TAG, "No account type key");
                        e.onError(ChatError.getError(ChatError.Code.NO_LOGIN_INFO));
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user!=null)
                    {
                        e.onSuccess(user);
                    }
                    else{
                        e.onError(ChatError.getError(ChatError.Code.NO_AUTH_DATA, "No auth data found"));
                    }
                }
            }
        }).flatMapCompletable(new Function<FirebaseUser, Completable>() {
            @Override
            public Completable apply(FirebaseUser firebaseUser) throws Exception {
                return handleFAUser(firebaseUser);
            }
        }).doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                // Whether we complete successfully or not, we set the status to idle
                setAuthStateToIdle();
            }
        });
    }

    @Override
    public Completable authenticateWithMap(final Map<String, Object> details) {

        final int loginType = (Integer)details.get(LoginType.TypeKey);

        Completable c =  Single.create(new SingleOnSubscribe<FirebaseUser>() {
            @Override
            public void subscribe(final SingleEmitter<FirebaseUser> e) throws Exception {
                if (DEBUG) Timber.v("authenticateWithMap, KeyType: %s", details.get(LoginType.TypeKey));

                if (isAuthenticating())
                {
                    if (DEBUG) Timber.d("Already Authenticating!, Status: %s", getAuthStatus().name());

                    e.onError(ChatError.getError(ChatError.Code.AUTH_IN_PROCESS, "Can't run two auth in parallel"));
                    return;
                }

                setAuthStatus(AuthStatus.AUTH_WITH_MAP);

                OnCompleteListener<AuthResult> resultHandler = new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if(task.isComplete() && task.isSuccessful()) {

                            FirebaseUser user = task.getResult().getUser();

                            // Save the authentication ID for the current user
                            // Set the current user
                            final Map<String, Object> loginInfoMap = new HashMap<String, Object>();

                            String uid = user.getUid();
                            if (DEBUG) Timber.v("Uid: " + uid);

                            loginInfoMap.put(Defines.Prefs.AccountTypeKey, loginType);
                            loginInfoMap.put(Defines.Prefs.AuthenticationID, uid);
                            //loginInfoMap.put(Defines.Prefs.TokenKey, );

                            setLoginInfo(loginInfoMap);

                            e.onSuccess(task.getResult().getUser());
                        } else {
                            e.onError(ChatError.getExceptionError(task.getException()));
                        }
                    }
                };

                AuthCredential credential = null;

                String password = (String) details.get(LoginType.PasswordKey);
                String email = (String) details.get(LoginType.EmailKey);

                switch (loginType)
                {
                    case AccountType.Facebook:

                        String accessToken = BFacebookManager.userFacebookAccessToken;

                        addLoginInfoData(Defines.Prefs.TokenKey, accessToken);

                        if (DEBUG) Timber.d(TAG, "Authenticating with fb, AccessToken: %s", accessToken);

                        credential = FacebookAuthProvider.getCredential(accessToken);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);
                        break;

                    case AccountType.Twitter:
                        String token = TwitterManager.accessToken.getToken();
                        String secret = TwitterManager.accessToken.getSecret();

                        addLoginInfoData(Defines.Prefs.TokenKey, token);

                        if (DEBUG) Timber.d("authing with twitter, AccessToken: %s", token);

                        credential = TwitterAuthProvider.getCredential(token, secret);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                        break;

                    case AccountType.Password:
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(resultHandler);
                        break;
                    case  AccountType.Register:
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(resultHandler);
                        break;
                    case AccountType.Anonymous:
                        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(resultHandler);
                        break;
                    case AccountType.Custom:
                        FirebaseAuth.getInstance().signInWithCustomToken((String) details.get(Defines.Prefs.TokenKey)).addOnCompleteListener(resultHandler);
                        break;
                    default:
                        if (DEBUG) Timber.d("No login type was found");
                        e.onError(ChatError.getError(ChatError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                        break;
                }
            }
        }).flatMapCompletable(new Function<FirebaseUser, Completable>() {
            @Override
            public Completable apply(FirebaseUser firebaseUser) throws Exception {
                return handleFAUser(firebaseUser);
            }
        }).doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                // Whether we complete successfully or not, we set the status to idle
                setAuthStateToIdle();
            }
        });
        // TODO: Need to look at this - how to make sure it always executes...
        //c.subscribe();

        return c;
    }

    private Completable handleFAUser(final FirebaseUser authData){
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                if (DEBUG) Timber.v("handleFAUser");

                setAuthStatus(AuthStatus.HANDLING_F_USER);

                if (authData == null) {
                    // If the user isn't authenticated they'll need to login
                    e.onError(ChatError.getError(ChatError.Code.SESSION_CLOSED));
                }
                else {

                    // Do a once() on the user to push its details to firebase.
                    final BUserWrapper wrapper = BUserWrapper.initWithAuthData(authData);
                    wrapper.once().then(new DoneCallback<BUser>() {
                        @Override
                        public void onDone(BUser user) {

                            if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");
                            DaoCore.updateEntity(user);

                            StateManager.shared().userOn(user.getEntityID());

                            // TODO push a default image of the user to the cloud.
                            // TODO: This shouldn't return the error... Would lead to a race condition
                            if(!NetworkManager.shared().a.push.subscribeToPushChannel(wrapper.pushChannel())) {
                                // TODO: Handle this error
                                Timber.v(ChatError.getError(ChatError.Code.BACKENDLESS_EXCEPTION));
                                //e.onError(ChatError.getError(ChatError.Code.BACKENDLESS_EXCEPTION));
                            }

                            BNetworkManager.getCoreInterface().goOnline();

                            wrapper.push().done(new DoneCallback<BUser>() {
                                @Override
                                public void onDone(BUser u) {
                                    if (DEBUG) Timber.v("OnDone, user was pushed from firebase.");
                                    e.onComplete();
                                }
                            }).fail(new FailCallback<ChatError>() {
                                @Override
                                public void onFail(ChatError error) {
                                    e.onError(error);
                                }
                            });
                        }
                    }, new FailCallback<ChatError>() {
                        @Override
                        public void onFail(ChatError chatError) {
                            e.onError(chatError);
                        }
                    });
                }
            }
        });
    }

    public Boolean userAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public Completable changePassword(String email, String oldPassword, final String newPassword) {
        Completable c = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                OnCompleteListener<Void> resultHandler = new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            e.onComplete();
                        } else {
                            e.onError(getFirebaseError(DatabaseError.fromException(task.getException())));
                        }
                    }
                };

                user.updatePassword(newPassword).addOnCompleteListener(resultHandler);
            }
        });
        c.subscribe();
        return c;
    }

    public Completable logout() {
        BUser user = BNetworkManager.getCoreInterface().currentUserModel();

        // Stop listening to user related alerts. (added message or thread.)
        StateManager.shared().userOff(user.getEntityID());

        // Removing the push channel
        if (NetworkManager.shared().a.push != null)
            NetworkManager.shared().a.push.unsubscribeToPushChannel(user.getPushChannel());

        // Login out
        // TODO: Move this to the user wrapper
        DatabaseReference userOnlineRef = FirebasePaths.userOnlineRef(user.getEntityID());
        userOnlineRef.setValue(false);

        FirebaseAuth.getInstance().signOut();

        return Completable.complete();
    }

    public Completable sendPasswordResetMail(final String email) {
        Completable c = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                OnCompleteListener<Void> resultHandler = new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(DEBUG) Timber.v("Email sent");
                            e.onComplete();
                        } else {
                            e.onError(getFirebaseError(DatabaseError.fromException(task.getException())));
                        }
                    }
                };

                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(resultHandler);

            }
        });
        c.subscribe();
        return c;
    }

    public Boolean accountTypeEnabled(AccountType type) {
        return null;
    }



}
