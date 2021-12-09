package sdk.chat.firebase.adapter;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.base.AbstractCoreHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class FirebaseCoreHandler extends AbstractCoreHandler {

    protected static FirebaseDatabase database;

    public FirebaseCoreHandler() {
        database();

        AppBackgroundMonitor.shared().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {
                sendAvailablePresence().subscribe();
            }

            @Override
            public void didStop() {
                sendUnavailablePresence().subscribe();
                ChatSDK.core().save();
                ChatSDK.hook().executeHook(HookEvent.UserWillDisconnect, null);
            }
        });
    }

    public Completable pushUser() {
        return Completable.defer(() -> FirebaseModule.config().provider.userWrapper(ChatSDK.currentUser()).push());
    }

    @Override
    public Completable sendAvailablePresence() {
        return Completable.defer(() -> {
            if (!ChatSDK.config().disablePresence) {
                if (ChatSDK.auth() != null && ChatSDK.auth().isAuthenticatedThisSession()) {
                    return FirebaseModule.config().provider.userWrapper(currentUser()).goOnline();
                }
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable sendUnavailablePresence() {
        return Completable.defer(() -> {
            if (!ChatSDK.config().disablePresence) {
                if (ChatSDK.auth() != null && ChatSDK.auth().isAuthenticatedThisSession()) {
                    return FirebaseModule.config().provider.userWrapper(currentUser()).goOffline();
                }
            }
            return Completable.complete();
        });
    }

    public Completable setUserOnline() {
        return Completable.defer(() -> {
            if (!ChatSDK.config().disablePresence) {
                User current = ChatSDK.currentUser();
                if (current != null && !current.getEntityID().isEmpty()) {
                    return FirebaseModule.config().provider.userWrapper(currentUser()).goOnline();
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> {
            if (ChatSDK.hook() != null) {
                ChatSDK.hook().executeHook(HookEvent.UserDidConnect, null).subscribe(ChatSDK.events());
            }
        }).subscribeOn(RX.db());
    }

//    public Completable setUserOffline() {
//        return Completable.defer(() -> {
//            if (!ChatSDK.config().disablePresence && ChatSDK.auth().isAuthenticated()) {
//
//                final User current = ChatSDK.currentUser();
//
//                if (current != null && !current.getEntityID().isEmpty()) {
//                    // Update the last online figure then go offline
//                    return updateLastOnline().concatWith(UserWrapper.initWithModel(current).goOffline());
//                }
//            }
//            return Completable.complete();
//        });
//    }

//    public void goOffline() {
//        ChatSDK.core().save();
//
//        Completable hookExecute;
//        if (ChatSDK.hook() != null) {
//            hookExecute = ChatSDK.hook().executeHook(HookEvent.UserWillDisconnect, null);
//        } else {
//            hookExecute = Completable.complete();
//        }
//
//        hookExecute.concatWith(setUserOffline()).doOnComplete(() -> {
//            FirebaseCoreHandler.database().goOffline();
//        }).subscribe(ChatSDK.events());
//
//    }

    public Completable updateLastOnline() {
        return Completable.defer(() -> {
            if (ChatSDK.lastOnline() != null) {
                return ChatSDK.lastOnline().updateLastOnline();
            }
            return Completable.complete();
        });
    }

    public Completable userOn(final User user) {
        return FirebaseModule.config().provider.userWrapper(user).on();
    }

    public void userOff(final User user) {
        FirebaseModule.config().provider.userWrapper(user).off();
    }

    public void save() {

    }

    @Override
    public Single<User> getUserForEntityID(String entityID) {
        return Single.defer(() -> {
            final User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
            return userOn(user).toSingle(() -> user);
        }).subscribeOn(RX.db());
    }

    @Override
    public User getUserNowForEntityID(String entityID) {
        final User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
        userOn(user).subscribe(ChatSDK.events());
        return user;
    }

    public static FirebaseApp app () {
        String firebaseApp = FirebaseModule.config().firebaseApp;
        if (firebaseApp != null) {
            return FirebaseApp.getInstance(firebaseApp);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    public static FirebaseAuth auth () {
        return FirebaseAuth.getInstance(app());
    }

    public static FirebaseDatabase database () {
        if (database == null) {
            if (FirebaseModule.config().firebaseDatabaseUrl != null) {
                database = FirebaseDatabase.getInstance(app(), FirebaseModule.config().firebaseDatabaseUrl);
            } else {
                database = FirebaseDatabase.getInstance(app());
            }
            database.setPersistenceEnabled(true);
        }
        return database;
    }
}
