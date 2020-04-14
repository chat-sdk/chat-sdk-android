package co.chatsdk.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.pmw.tinylog.Logger;

import java.util.Date;
import java.util.concurrent.Callable;

import io.reactivex.CompletableSource;
import sdk.chat.core.base.AbstractCoreHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.firebase.module.FirebaseModule;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import sdk.guru.realtime.RealtimeEventListener;


/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class FirebaseCoreHandler extends AbstractCoreHandler {

    protected static FirebaseDatabase database;

    public FirebaseCoreHandler() {
        database();
    }

    public Completable pushUser() {
        return Completable.defer(() -> new UserWrapper(ChatSDK.currentUser()).push()).subscribeOn(Schedulers.io());
    }

    public Completable setUserOnline() {
        return Completable.defer(() -> {
            if (!ChatSDK.config().disablePresence) {
                User current = ChatSDK.currentUser();
                if (current != null && !current.getEntityID().isEmpty()) {
                    return UserWrapper.initWithModel(currentUser()).goOnline();
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> {
            if (ChatSDK.hook() != null) {
                ChatSDK.hook().executeHook(HookEvent.UserDidConnect, null).subscribe(ChatSDK.events());
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable setUserOffline() {
        return Completable.defer(() -> {
            if (!ChatSDK.config().disablePresence) {

                final User current = ChatSDK.currentUser();

                Completable completable = Completable.complete();
                if (ChatSDK.hook() != null) {
                    completable = ChatSDK.hook().executeHook(HookEvent.UserWillDisconnect, null);
                }

                if (current != null && !current.getEntityID().isEmpty()) {
                    // Update the last online figure then go offline
                    return completable.concatWith(updateLastOnline()
                            .concatWith(UserWrapper.initWithModel(current).goOffline()));
                }
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    public void goOffline() {
        ChatSDK.core().save();
        ChatSDK.events().disposeOnLogout(setUserOffline().subscribe(DatabaseReference::goOffline));
    }

    public void goOnline() {
        super.goOnline();
        FirebasePaths.firebaseRawRef().child(".info/connected").addListenerForSingleValueEvent(new RealtimeEventListener().onValue((snapshot, hasValue) -> {
            if (hasValue) {
                Logger.debug("Already online!");
            } else {
                DatabaseReference.goOnline();
            }
            setUserOnline().subscribe(ChatSDK.events());
        }));
    }

    public Completable updateLastOnline() {
        return Completable.defer(() -> {
            if (ChatSDK.lastOnline() != null) {
                return ChatSDK.lastOnline().setLastOnline(currentUser());
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    public Completable userOn(final User user) {
        return new UserWrapper(user).on();
    }

    public void userOff(final User user) {
        new UserWrapper(user).off();
    }

    public void save() {

    }

    @Override
    public Single<User> getUserForEntityID(String entityID) {
        return Single.defer(() -> {
            final User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
            return userOn(user).toSingle(() -> user);
        });
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
