package co.chatsdk.firebase;

import android.support.annotation.NonNull;

import co.chatsdk.core.NM;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AuthKeys;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.DaoCore;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static co.chatsdk.firebase.FirebaseErrors.getFirebaseError;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseAuthenticationHandler extends AbstractAuthenticationHandler {

    private static boolean DEBUG = Debug.FirebaseAuthenticationHandler;

    public Completable authenticateWithCachedToken() {
        return Single.create(new SingleOnSubscribe<FirebaseUser>() {
            @Override
            public void subscribe(final SingleEmitter<FirebaseUser> e) throws Exception {

                if (isAuthenticating()) {
                    if (DEBUG) Timber.d("Already Authing!, Status: %s", getAuthStatus().name());
                    e.onError(ChatError.getError(ChatError.Code.AUTH_IN_PROCESS, "Cant run two auth in parallel"));
                }
                else {
                    setAuthStatus(AuthStatus.CHECKING_IF_AUTH);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        e.onSuccess(user);
                    }
                    else {
                        e.onError(ChatError.getError(ChatError.Code.NO_AUTH_DATA, "No auth bundle found"));
                    }
                }
            }
        }).flatMapCompletable(new Function<FirebaseUser, Completable>() {
            @Override
            public Completable apply(FirebaseUser firebaseUser) throws Exception {
                return authenticateWithUser(firebaseUser);
            }
        }).doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                // Whether we complete successfully or not, we set the status to idle
                setAuthStateToIdle();
            }
        }).subscribeOn(Schedulers.single());
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
                            e.onSuccess(task.getResult().getUser());
                        }
                        else {
                            task.getException().printStackTrace();
                            e.onError(task.getException());
                        }
                    }
                };

                switch (details.type)
                {
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
                    // Should be handled by Social Login Module
                    case Facebook:
                    case Twitter:
                    default:
                        if (DEBUG) Timber.d("No login type was found");
                        e.onError(ChatError.getError(ChatError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                        break;
                }
            }
        }).flatMapCompletable(new Function<FirebaseUser, Completable>() {
            @Override
            public Completable apply(FirebaseUser firebaseUser) throws Exception {
                return authenticateWithUser(firebaseUser);
            }
        }).doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                // Whether we complete successfully or not, we set the status to idle
                setAuthStateToIdle();
            }
        }).subscribeOn(Schedulers.single());
    }

    public Completable authenticateWithUser (final FirebaseUser user) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final Map<String, Object> loginInfoMap =  new HashMap<>();
                // Save the authentication ID for the current user
                // Set the current user

                String uid = user.getUid();

                loginInfoMap.put(AuthKeys.CurrentUserID, uid);

                setLoginInfo(loginInfoMap);

                setAuthStatus(AuthStatus.HANDLING_F_USER);

                // Do a once() on the user to push its details to firebase.
                final UserWrapper wrapper = UserWrapper.initWithAuthData(user);

                wrapper.once().subscribe(new Action() {
                    @Override
                    public void run() throws Exception {

                        if (DEBUG) Timber.v("OnDone, user was pulled from firebase.");
                        wrapper.getModel().update();

                        FirebaseEventHandler.shared().userOn(wrapper.getModel().getEntityID());

                        NM.push().subscribeToPushChannel(wrapper.pushChannel());

                        wrapper.push().subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                e.onComplete();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        e.onError(throwable);
                    }
                });

            }
        }).subscribeOn(Schedulers.single());
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
        }).subscribeOn(Schedulers.single());
    }

    public Completable logout() {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {

                final User user = NM.currentUser();

                // Stop listening to user related alerts. (added message or thread.)
                FirebaseEventHandler.shared().userOff(user.getEntityID());

                // Removing the push channel
                if (NM.push() != null) {
                    NM.push().unsubscribeToPushChannel(user.getPushChannel());
                }

                NM.core().setUserOffline().subscribe(new Action() {
                    @Override
                    public void run() throws Exception {

                        FirebaseAuth.getInstance().signOut();

                        NM.events().source().onNext(NetworkEvent.logout());

                        if(NM.socialLogin() != null) {
                            NM.socialLogin().logout();
                        }

                        if(NM.hook() != null) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put(BaseHookHandler.Logout, user);
                            NM.hook().executeHook(BaseHookHandler.Logout_User, data);
                        }

                        e.onComplete();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        e.onError(throwable);
                    }
                });

            }
        }).subscribeOn(Schedulers.single());
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
        }).subscribeOn(Schedulers.single());
    }

    // TODO: Allow users to turn anonymous login off or on in settings
    public Boolean accountTypeEnabled(AccountDetails.Type type) {
        if(NM.socialLogin() != null) {
            return NM.socialLogin().accountTypeEnabled(type);
        }
        else {
            if(type == AccountDetails.Type.Anonymous || type == AccountDetails.Type.Username || type == AccountDetails.Type.Register) {
                return true;
            }
        }
        return false;
    }





}
