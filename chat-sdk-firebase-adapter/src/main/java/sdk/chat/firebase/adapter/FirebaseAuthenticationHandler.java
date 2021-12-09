package sdk.chat.firebase.adapter;

import android.os.AsyncTask;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.pmw.tinylog.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.base.AbstractAuthenticationHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.utils.Generic;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;
import sdk.guru.common.EventType;
import sdk.guru.common.RX;
import sdk.guru.realtime.RXRealtime;
import sdk.guru.realtime.RealtimeReferenceManager;

/**
 * Created by benjaminsmiley-andrews on 03/05/2017.
 */

public class FirebaseAuthenticationHandler extends AbstractAuthenticationHandler {

    protected FirebaseAuth.AuthStateListener authStateListener;

    public FirebaseAuthenticationHandler() {
        // Handle login and log out automatically
        authStateListener = firebaseAuth -> {
            if (ChatSDK.shared().isValid() && !isAuthenticating() && loggingOut == null) {
                // We are logged in with Firebase
                if (firebaseAuth.getCurrentUser() != null) {
                    // We are logged in with the wrong user
                    if (isAuthenticatedThisSession() && !currentUserID.equals(firebaseAuth.getCurrentUser().getUid())) {
                        logout().concatWith(authenticate()).subscribe(ChatSDK.events());
                    } else {
                        authenticate().subscribe(ChatSDK.events());
                    }
                } else {
                    logout().subscribe(ChatSDK.events());
                }
            }
        };
        FirebaseAuth.getInstance(FirebaseCoreHandler.app()).addAuthStateListener(authStateListener);
    }

    @Override
    public Completable authenticate() {
        return Completable.defer(() -> {
            if (isAuthenticating()) {
                return authenticating.cache();
            }
            if (isAuthenticatedThisSession()) {
                return Completable.complete();
            }
            if (!isAuthenticated()) {
                return Completable.error(ChatSDK.getException(R.string.authentication_required));
            }

            authenticating = authenticateWithUser(FirebaseCoreHandler.auth().getCurrentUser());
            return authenticating;
        }).doFinally(this::cancel);
    }

    @Override
    public Completable authenticate(final AccountDetails details) {
        return Completable.defer(() -> {
            if (isAuthenticating()) {
                return authenticating;
            }
            else if (isAuthenticated()) {
                return Completable.complete();
            }
            authenticating = Single.create((SingleOnSubscribe<FirebaseUser>) emitter -> {

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
                        emitter.onError(ChatSDK.getException(R.string.no_login_type_defined));
                        break;
                }
            }).subscribeOn(RX.io()).flatMapCompletable(this::authenticateWithUser).cache();

            return authenticating;
        }).doFinally(this::cancel);
    }

    public Boolean cachedCredentialsAvailable() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public Completable retrieveRemoteConfig() {
        return Completable.create(emitter -> {
            if (ChatSDK.config().remoteConfigEnabled) {
                RXRealtime realtime = new RXRealtime();
                realtime.on(FirebasePaths.configRef()).doOnNext(change -> {
                    if (change.getType() != EventType.Removed) {
                        Map<String, Object> map = change.getSnapshot().getValue(Generic.mapStringObject());
                        if (map != null) {
                            ChatSDK.config().setRemoteConfig(map);
                        }
                    } else {
                        ChatSDK.config().clearRemoteConfig();
                    }
                    emitter.onComplete();
                }).ignoreElements().subscribe(ChatSDK.events());
                realtime.addToReferenceManager();
            } else {
                emitter.onComplete();
            }
        }).subscribeOn(RX.io());
    }

    public Completable authenticateWithUser(final FirebaseUser user) {
        return Completable.merge(Arrays.asList(Completable.defer(() -> {

            ChatSDK.db().openDatabase(user.getUid());

            Logger.info("authenticateWithUser: " + user.getUid());

            User cachedUser = ChatSDK.db().fetchUserWithEntityID(user.getUid());

            if (cachedUser != null && (!FirebaseModule.config().developmentModeEnabled || isAuthenticatedThisSession())) {
                completeAuthentication(cachedUser);
                return Completable.complete();
            }

            // Do a once() on the user to push its details to firebase.
            UserWrapper userWrapper = FirebaseModule.config().provider.userWrapper(user);
            return userWrapper.push().doOnComplete(() -> {
                completeAuthentication(userWrapper.getModel());
            });

        }).doFinally(this::setAuthStateToIdle).subscribeOn(RX.db()), retrieveRemoteConfig())).cache();
    }

    protected void completeAuthentication(User user) {

        setCurrentUserEntityID(user.getEntityID());

        ChatSDK.events().impl_currentUserOn(user.getEntityID());

        if (ChatSDK.hook() != null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put(HookEvent.User, user);
            ChatSDK.hook().executeHook(HookEvent.DidAuthenticate, data).subscribe(ChatSDK.events());
        }


        ChatSDK.core().sendAvailablePresence().subscribe();

        Logger.info("Authentication complete! name = " +user.getName()+ ", id = " + user.getEntityID());

        setAuthStateToIdle();

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
                .subscribeOn(RX.io());
    }

    public Completable logout() {
        return Completable.defer(() -> {
            if (loggingOut != null) {
                return loggingOut;
            }
            else if (!isAuthenticated() && currentUserID == null) {
                return Completable.complete();
            }
            loggingOut = ChatSDK.hook().executeHook(HookEvent.WillLogout)
                    .concatWith(ChatSDK.core().sendUnavailablePresence())
                    .concatWith(Completable.defer(() -> {

                        final User user = ChatSDK.currentUser();

                        Logger.info("Logout complete! name = " +user.getName()+ ", id = " + user.getEntityID());

                        // Stop listening to user related alerts. (added text or thread.)
                        ChatSDK.events().impl_currentUserOff(user.getEntityID());

                        RealtimeReferenceManager.shared().removeAllListeners();

                        FirebaseCoreHandler.auth().signOut();

                        clearCurrentUserEntityID();

                        ChatSDK.events().source().accept(NetworkEvent.logout());

                        ChatSDK.db().closeDatabase();
                        RealtimeReferenceManager.shared().clear();

                        if (ChatSDK.hook() != null) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put(HookEvent.User, user);
                            return ChatSDK.hook().executeHook(HookEvent.DidLogout, data);
                        } else {
                            return Completable.complete();
                        }
                    }).subscribeOn(RX.computation()));
            return loggingOut.doFinally(this::setAuthStateToIdle);
        });
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

                }).subscribeOn(RX.io());
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

    @Override
    public void stop() {
        super.stop();
        if (authStateListener != null) {
            FirebaseAuth.getInstance(FirebaseCoreHandler.app()).removeAuthStateListener(authStateListener);
        }
    }

}
