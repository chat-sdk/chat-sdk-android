package co.chatsdk.firebase;

import android.os.AsyncTask;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.concurrent.Callable;

import co.chatsdk.core.base.AbstractAuthenticationHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.ChatError;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.firebase.utils.Generic;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseAuthenticationHandler extends AbstractAuthenticationHandler {

    DisposableMap dm = new DisposableMap();

    public FirebaseAuthenticationHandler() {

        // Handle login and log out automatically
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            // We are connecting for the first time
            if (this.currentUserID == null && firebaseAuth.getCurrentUser() != null) {
                if (!isAuthenticating()) {
                    dm.add(authenticate().subscribe(() -> {}, ChatSDK.events()));
                }
            }
            if(this.currentUserID != null && firebaseAuth.getCurrentUser() == null) {
                if (isAuthenticated()) {
                    dm.add(logout().subscribe(() -> {}, ChatSDK.events()));
                }
            }
        });

    }

    public Completable authenticate() {
        return Single.create((SingleOnSubscribe<FirebaseUser>) emitter-> {

            if (isAuthenticating()) {
                emitter.onError(ChatError.getError(ChatError.Code.AUTH_IN_PROCESS, "Cant execute two auth in parallel"));
            } else {

                setIsAuthenticating(true);

                FirebaseUser user = FirebaseCoreHandler.auth().getCurrentUser();

                if (user != null) {
                    emitter.onSuccess(user);

                } else {
                    emitter.onError(ChatError.getError(ChatError.Code.NO_AUTH_DATA, "No auth bundle found"));
                }
            }

        })
        .flatMapCompletable(this::authenticateWithUser)
        .doOnTerminate(this::setAuthStateToIdle) // Whether we complete successfully or not, we set the status to Idle
        .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable authenticate(final AccountDetails details) {
        return Single.create((SingleOnSubscribe<FirebaseUser>) emitter -> {
                    if (isAuthenticating()) {
                        emitter.onError(ChatError.getError(ChatError.Code.AUTH_IN_PROCESS, "Can't execute two auth in parallel"));
                        return;
                    }

                    setIsAuthenticating(true);

                    OnCompleteListener<AuthResult> resultHandler = task->AsyncTask.execute(()-> {
                        if (task.isComplete() && task.isSuccessful() && task.getResult() != null) {
                            emitter.onSuccess(task.getResult().getUser());
                        } else {
                            emitter.onError(task.getException());
                        }
                    });

                    switch (details.type) {
                        case Username:
                            FirebaseCoreHandler.auth().signInWithEmailAndPassword(details.username, details.password).addOnCompleteListener(resultHandler);
                            break;
                        case Register:
                            FirebaseCoreHandler.auth().createUserWithEmailAndPassword(details.username, details.password).addOnCompleteListener(resultHandler);
                            break;
                        case Anonymous:
                            FirebaseCoreHandler.auth().signInAnonymously().addOnCompleteListener(resultHandler);
                            break;
                        case Custom:
                            FirebaseCoreHandler.auth().signInWithCustomToken(details.token).addOnCompleteListener(resultHandler);
                            break;
                        default:
                            emitter.onError(ChatError.getError(ChatError.Code.NO_LOGIN_TYPE, "No matching login type was found"));
                            break;
                    }
                })
                .flatMapCompletable(this::authenticateWithUser)
                .doOnTerminate(this::setAuthStateToIdle)
                .subscribeOn(Schedulers.io());
    }

    public Completable retrieveRemoteConfig() {
        return Completable.create(emitter -> {
            if (ChatSDK.config().remoteConfigEnabled) {
                FirebasePaths.configRef().addListenerForSingleValueEvent(new FirebaseEventListener().onValue((snapshot, hasValue) -> {
                    if (hasValue && snapshot.getValue() instanceof HashMap) {
                        HashMap<String, Object> map = snapshot.getValue(Generic.hashMapStringObject());
                        if (map != null) {
                            ChatSDK.config().updateRemoteConfig(map);
                        }
                    }
                    emitter.onComplete();
                }).onCancelled(error -> emitter.onError(error.toException())));
            } else {
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable authenticateWithUser(final FirebaseUser user) {
        return Completable.defer(() -> {


            String uid = user.getUid();

            saveCurrentUserEntityID(uid);

            // Do a once() on the user to push its details to firebase.
            UserWrapper userWrapper = UserWrapper.initWithAuthData(user);
            return userWrapper.push().concatWith(userWrapper.on()).doOnComplete(() -> {

                ChatSDK.events().impl_currentUserOn(userWrapper.getModel().getEntityID());

                if (ChatSDK.hook() != null) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(HookEvent.User, userWrapper.getModel());
                    ChatSDK.hook().executeHook(HookEvent.DidAuthenticate, data).subscribe(ChatSDK.events());
                }

                ChatSDK.core().setUserOnline().subscribe(ChatSDK.events());

                authenticatedThisSession = true;
            });
        }).andThen(retrieveRemoteConfig()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Boolean isAuthenticated() {
        return FirebaseCoreHandler.auth().getCurrentUser() != null;
    }

    @Override
    public Completable changePassword(String email, String oldPassword, final String newPassword) {
        return Completable.create(
                emitter->{
                    FirebaseUser user = FirebaseCoreHandler.auth().getCurrentUser();

                    OnCompleteListener<Void> resultHandler = task->{
                        if (task.isSuccessful()) {
                            emitter.onComplete();
                        } else {
                            emitter.onError(task.getException());
                        }
                    };

                    user.updatePassword(newPassword).addOnCompleteListener(resultHandler);
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable logout() {
        return ChatSDK.hook().executeHook(HookEvent.WillLogout)
                .concatWith(ChatSDK.core().setUserOffline())
                .concatWith(Completable.defer(() -> {

                    final User user = ChatSDK.currentUser();

                    // Stop listening to user related alerts. (added text or thread.)
                    ChatSDK.events().impl_currentUserOff(user.getEntityID());

                    FirebaseCoreHandler.auth().signOut();

                    clearSavedCurrentUserEntityID();

                    ChatSDK.events().source().onNext(NetworkEvent.logout());

                    if (ChatSDK.hook() != null) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put(HookEvent.User, user);
                        return ChatSDK.hook().executeHook(HookEvent.DidLogout, data);
                    } else {
                        return Completable.complete();
                    }
                })).subscribeOn(Schedulers.io());
    }

    public Completable sendPasswordResetMail(final String email) {
        return Completable.create(
                emitter->{
                    OnCompleteListener<Void> resultHandler = task->{
                        if (task.isSuccessful()) {
                            emitter.onComplete();
                        } else {
                            emitter.onError(task.getException());
                        }
                    };

                    FirebaseCoreHandler.auth().sendPasswordResetEmail(email).addOnCompleteListener(resultHandler);

                }).subscribeOn(Schedulers.io());
    }

    // TODO: Allow users to turn anonymous login off or on in settings
    public Boolean accountTypeEnabled(AccountDetails.Type type) {
        if (type == AccountDetails.Type.Anonymous) {
            return ChatSDK.config().anonymousLoginEnabled;
        } else if (type == AccountDetails.Type.Username || type == AccountDetails.Type.Register) {
            return true;
        } else {
            return false;
        }
    }


}
