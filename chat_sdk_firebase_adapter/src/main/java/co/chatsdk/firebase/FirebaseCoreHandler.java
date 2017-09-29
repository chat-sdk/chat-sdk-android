package co.chatsdk.firebase;

import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static co.chatsdk.firebase.FirebaseErrors.getFirebaseError;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class FirebaseCoreHandler extends AbstractCoreHandler {

    private UserWrapper currentUser(){
        return UserWrapper.initWithModel(currentUserModel());
    }
    private DisposableList disposableList = new DisposableList();

    public FirebaseCoreHandler () {
        // When the user logs out, turn off all the existing listeners
        FirebaseEventHandler.shared().source()
                .filter(NetworkEvent.filterType(EventType.Logout))
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(NetworkEvent networkEvent) throws Exception {
                disposableList.dispose();
            }
        });
    }

    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public Completable pushUser () {
        return currentUser().push();
    }

    public Completable setUserOnline() {

        DatabaseReference.goOffline();

        User current = NM.currentUser();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            return UserWrapper.initWithModel(currentUserModel()).goOnline();
        }
        return Completable.complete();
    }

    public Completable setUserOffline() {
        User current = NM.currentUser();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            // Update the last online figure then go offline
            return updateLastOnline()
                    .concatWith(UserWrapper.initWithModel(currentUserModel()).goOffline());
        }
        return Completable.complete();
    }

    public void goOffline() {
        NM.core().save();
        disposableList.add(setUserOffline().subscribe(new Action() {
            @Override
            public void run() throws Exception {
                DatabaseReference.goOffline();
            }
        }));
    }

    public void goOnline() {
        DatabaseReference.goOnline();
        disposableList.add(setUserOnline().subscribe());
    }

    public Completable updateLastOnline () {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                User currentUser = NM.currentUser();
                currentUser.setLastOnline(new Date());
                currentUser.update();
                e.onComplete();
            }
        }).concatWith(pushUser()).subscribeOn(Schedulers.single());
    }

    public Single<Boolean> isOnline() {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> e) throws Exception {
                if (NM.currentUser() == null) {
                    e.onError(ChatError.getError(ChatError.Code.NULL, "Current user is null"));
                    return;
                }

                FirebasePaths.userOnlineRef(NM.currentUser().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        disposableList.add(updateLastOnline().subscribe());

                        e.onSuccess((Boolean) snapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        e.onError(getFirebaseError(firebaseError));
                    }
                });
            }
        }).subscribeOn(Schedulers.single());
    }

    public void presenceMonitoringOn (final User user) {
        disposableList.add(new UserWrapper(user).onlineOn().subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                NM.events().source().onNext(NetworkEvent.userPresenceUpdated(user));
            }
        }));
    }

    public void presenceMonitoringOff (final User user) {
        new UserWrapper(user).onlineOff();
    }

    public void save () {

    }
}
