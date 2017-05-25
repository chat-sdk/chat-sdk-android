package co.chatsdk.firebase;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebasePaths;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.ChatError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.core.BMessage;
import co.chatsdk.core.dao.core.BUser;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;

import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import timber.log.Timber;

import static com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseErrors.getFirebaseError;

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
        String entityID = NetworkManager.shared().a.auth.getCurrentUserEntityID();

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
        BUser current = NetworkManager.shared().a.core.currentUserModel();
        if (current != null && StringUtils.isNotEmpty(current.getEntityID()))
        {
            UserWrapper.initWithModel(currentUserModel()).goOnline();
        }
    }

    public void setUserOffline() {
        BUser current = NetworkManager.shared().a.core.currentUserModel();
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
        BUser currentUser  = NetworkManager.shared().a.core.currentUserModel();
        currentUser.setLastOnline(new Date());
        DaoCore.updateEntity(currentUser);
        return pushUser();
    }

    public Single<Boolean> isOnline(){
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final SingleEmitter<Boolean> e) throws Exception {
                if (NetworkManager.shared().a.core.currentUserModel() == null)
                {
                    e.onError(ChatError.getError(ChatError.Code.NULL, "Current user is null"));
                    return;
                }

                FirebasePaths.userOnlineRef(NetworkManager.shared().a.core.currentUserModel().getEntityID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public Observable<Thread> createThread (ArrayList<BUser> users, String name) {
        return null;
    }

    public Observable<Thread> createThread (ArrayList<BUser> users) {
        return null;
    }

    public Observable<BUser> addUsersToThread (ArrayList<BUser> users, Thread thread) {
        return null;
    }

    public Observable<BUser> removeUsersFromThread (ArrayList<BUser> users, Thread thread) {
        return null;
    }

    public Observable<Void> loadMoreMessagesForThread (Thread thread) {
        return null;
    }

    public Observable<Void> deleteThread (Thread thread) {
        return null;
    }

    public Observable<Void> leaveThread (Thread thread) {
        return null;
    }

    public Observable<Void> joinThread (Thread thread) {
        return null;
    }

    public Observable<Void> sendMessage(String text, String threadID)  {
        return null;
    }

    public Observable<Void> sendMessage (BMessage message) {
        return null;
    }

    public ArrayList<BMessage> messagesForThread (String threadID, boolean ascending)  {
        return null;
    }

    public ArrayList<Thread> threadsWithType (ThreadType type) {
        return null;
    }

    public void save() {

    }

    public void sendLocalSystemMessageWithTextAndThreadEntityID(String text, String threadID) {

    }

    public void sendLocalSystemMessageWithTextTypeThreadEntityID(String text, bSystemMessageType type, String threadID) {

    }

}
