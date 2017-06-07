package co.chatsdk.firebase;

import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import timber.log.Timber;

import static co.chatsdk.firebase.FirebaseErrors.getFirebaseError;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class FirebaseCoreHandler extends AbstractCoreHandler {

    // TODO: Check this
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    private UserWrapper currentUser(){
        return UserWrapper.initWithModel(currentUserModel());
    }

    public BUser currentUserModel(){
        String entityID = NM.auth().getCurrentUserEntityID();

        if (StringUtils.isNotEmpty(entityID))
        {
            BUser currentUser = DaoCore.fetchEntityWithEntityID(BUser.class, entityID);

            if(DEBUG) {
                if (currentUser == null) Timber.e("Current user is null");
                else if (StringUtils.isEmpty(currentUser.getEntityID()))
                    Timber.e("Current user entity id is null");
            }

            return currentUser;
        }
        if (DEBUG) Timber.e("getCurrentUserAuthenticationIdr is null");
        return null;
    }

    /** Unlike the iOS code the current user need to be saved before you call this method.*/
    public Completable pushUser () {
        Completable c = currentUser().push();
        c.subscribe();
        return c;
    }

    public void setUserOnline() {
        BUser current = NM.currentUser();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            UserWrapper.initWithModel(currentUserModel()).goOnline();
        }
    }

    public void setUserOffline() {
        BUser current = NM.currentUser();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            UserWrapper.initWithModel(currentUserModel()).goOffline();
            updateLastOnline();
        }
    }

    public void goOffline() {
        DatabaseReference.goOffline();
        setUserOffline();
    }

    public void goOnline() {
        DatabaseReference.goOnline();
        setUserOnline();
    }

    public void observeUser(String entityID) {

    }

    public Completable updateLastOnline () {
        BUser currentUser  = NM.currentUser();
        currentUser.setLastOnline(new Date());
        DaoCore.updateEntity(currentUser);
        return pushUser();
    }

    public Single<Boolean> isOnline(){
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> e) throws Exception {
                if (NM.currentUser() == null)
                {
                    e.onError(ChatError.getError(ChatError.Code.NULL, "Current user is null"));
                    return;
                }

                FirebasePaths.userOnlineRef(NM.currentUser().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        updateLastOnline();

                        e.onSuccess((Boolean) snapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        e.onError(getFirebaseError(firebaseError));
                    }
                });
            }
        });
    }

    public void save () {

    }
}
