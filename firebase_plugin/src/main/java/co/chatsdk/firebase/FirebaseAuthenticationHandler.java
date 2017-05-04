package co.chatsdk.firebase;

import android.support.annotation.NonNull;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;

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

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
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
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import timber.log.Timber;

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

                        if (DEBUG) Timber.d(TAG, "authing with fb, AccessToken: %s", accessToken);

                        credential = FacebookAuthProvider.getCredential(accessToken);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                        break;

                    case AccountType.Twitter:
                        String token = TwitterManager.accessToken.getToken();
                        String secret = TwitterManager.accessToken.getSecret();

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
        c.subscribe();
        return c;
    }

    private Completable handleFAUser(final FirebaseUser authData){
        Completable c = Completable.create(new CompletableOnSubscribe() {
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
                        public void onDone(BUser bUser) {

                            if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");
                            DaoCore.updateEntity(bUser);

                            BNetworkManager.getCoreInterface().getEventManager().userOn(bUser);

                            // TODO push a default image of the user to the cloud.
                            // TODO: This shouldn't return the error... Would lead to a race condition
                            if(!BNetworkManager.getCoreInterface().getPushHandler().subscribeToPushChannel(wrapper.pushChannel())) {
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
        c.subscribe();
        return c;
    }

    public Boolean userAuthenticated() {
        return false;
    }




    public Observable<Void> logout() {
        return null;
    }

    public Boolean accountTypeEnabled(AccountType type) {
        return null;
    }

    public Map<String, Object> loginInfo() {
        return null;
    }

    public void setLoginInfo(Map<String, Object> info) {

    }

    public String currentUserEntityID() {
        return null;
    }

}
