package co.chatsdk.firebase;

import android.support.annotation.NonNull;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.DaoCore;
import com.braunster.chatsdk.network.FacebookManager;
import com.braunster.chatsdk.network.TwitterManager;
import co.chatsdk.core.types.ChatError;
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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.enums.AuthStatus;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.utils.AppContext;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static co.chatsdk.firebase.FirebaseErrors.getFirebaseError;

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
                        e.onError(ChatError.getError(ChatError.Code.NO_AUTH_DATA, "No auth bundle found"));
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
    public Completable authenticate (final AccountDetails details) {
        return Single.create(new SingleOnSubscribe<FirebaseUser>() {
            @Override
            public void subscribe(final SingleEmitter<FirebaseUser> e) throws Exception {

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

                            final Map<String, Object> loginInfoMap =  new HashMap<String, Object>();
                            // Save the authentication ID for the current user
                            // Set the current user

                            String uid = user.getUid();
                            if (DEBUG) Timber.v("Uid: " + uid);

                            loginInfoMap.put(Defines.Prefs.AccountTypeKey, details.type);
                            loginInfoMap.put(Defines.Prefs.AuthenticationID, uid);

                            setLoginInfo(loginInfoMap);

                            e.onSuccess(task.getResult().getUser());
                        } else {
                            e.onError(ChatError.getExceptionError(task.getException()));
                        }
                    }
                };

                AuthCredential credential;

                switch (details.type)
                {
                    case Facebook:

                        String accessToken = FacebookManager.userFacebookAccessToken;

                        addLoginInfoData(Defines.Prefs.TokenKey, accessToken);

                        if (DEBUG) Timber.d(TAG, "Authenticating with fb, AccessToken: %s", accessToken);

                        credential = FacebookAuthProvider.getCredential(accessToken);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);
                        break;

                    case Twitter:
                        String token = TwitterManager.accessToken.getToken();
                        String secret = TwitterManager.accessToken.getSecret();

                        addLoginInfoData(Defines.Prefs.TokenKey, token);

                        if (DEBUG) Timber.d("authing with twitter, AccessToken: %s", token);

                        credential = TwitterAuthProvider.getCredential(token, secret);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(resultHandler);

                        break;

                    case Username:
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(details.username, details.password).addOnCompleteListener(resultHandler);
                        break;
                    case  Register:
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(details.username, details.password).addOnCompleteListener(resultHandler);
                        break;
                    case Anonymous:
                        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(resultHandler);
                        break;
                    case Custom:
                        FirebaseAuth.getInstance().signInWithCustomToken(details.token).addOnCompleteListener(resultHandler);
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
    }

    private Completable handleFAUser(final FirebaseUser authData){

        if (DEBUG) Timber.v("handleFAUser");

        setAuthStatus(AuthStatus.HANDLING_F_USER);

        if (authData == null) {
            // If the user isn't authenticated they'll need to login
            return Completable.error(ChatError.getError(ChatError.Code.SESSION_CLOSED));
        }
        else {

            // Do a once() on the user to push its details to firebase.
            final UserWrapper wrapper = UserWrapper.initWithAuthData(authData);

            return wrapper.once().andThen(new CompletableSource() {
                @Override
                public void subscribe(CompletableObserver cs) {

                    if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");
                    DaoCore.updateEntity(wrapper.getModel());

                    FirebaseEventHandler.shared().userOn(wrapper.getModel().getEntityID());

                    // TODO push a default image of the user to the cloud.
                    // TODO: This shouldn't return the error... Would lead to a race condition
                    if(!NM.push().subscribeToPushChannel(wrapper.pushChannel())) {
                        // TODO: Handle this error
                        Timber.v(ChatError.getError(ChatError.Code.BACKENDLESS_EXCEPTION));
                        //e.onError(ChatError.getError(ChatError.Code.BACKENDLESS_EXCEPTION));
                    }

                    NM.core().goOnline();

                    cs.onComplete();

                }
            }).andThen(wrapper.push());
        }
    }

    public Boolean userAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    @Override
    public Completable changePassword(String email, String oldPassword, final String newPassword) {
        return Completable.create(new CompletableOnSubscribe() {
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
    }

    public Completable logout() {
        BUser user = NM.currentUser();

        // Stop listening to user related alerts. (added message or thread.)
        FirebaseEventHandler.shared().userOff(user.getEntityID());

        // Removing the push channel
        if (NM.push() != null)
            NM.push().unsubscribeToPushChannel(user.getPushChannel());

        // Login out
        // TODO: Move this to the user wrapper
        DatabaseReference userOnlineRef = FirebasePaths.userOnlineRef(user.getEntityID());
        userOnlineRef.setValue(false);

        FirebaseAuth.getInstance().signOut();

        NM.events().source().onNext(NetworkEvent.logout());

        return Completable.complete();
    }

    public Completable sendPasswordResetMail(final String email) {
        return Completable.create(new CompletableOnSubscribe() {
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
    }

    public Boolean accountTypeEnabled(int type) {
        if(type == AccountType.Facebook) {
            return facebookEnabled();
        }
        if(type == AccountType.Twitter) {
            return twitterEnabled();
        }
        if(type == AccountType.Google) {
            return googleEnabled();
        }
        return true;
    }

    public boolean facebookEnabled(){
        return StringUtils.isNotEmpty(AppContext.context.getString(com.braunster.chatsdk.R.string.facebook_id));
    }

    public boolean googleEnabled(){
        return false;
    }

    public boolean twitterEnabled(){
        return (StringUtils.isNotEmpty(AppContext.context.getString(com.braunster.chatsdk.R.string.twitter_consumer_key))
                && StringUtils.isNotEmpty(AppContext.context.getString(com.braunster.chatsdk.R.string.twitter_consumer_secret)))
                ||
               (StringUtils.isNotEmpty(AppContext.context.getString(com.braunster.chatsdk.R.string.twitter_access_token))
                        && StringUtils.isNotEmpty(AppContext.context.getString(com.braunster.chatsdk.R.string.twitter_access_token_secret)));
    }




}
