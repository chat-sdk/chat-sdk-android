package co.chatsdk.firebase;

import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.core.entities.Message;
import co.chatsdk.core.entities.ThreadType;
import co.chatsdk.core.entities.User;
import io.reactivex.Observable;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 02/05/2017.
 */

public class FirebaseCorehandler extends AbstractCoreHandler {

    // TODO: Check this
    private static boolean DEBUG = Debug.BFirebaseNetworkAdapter;

    private BUserWrapper currentUser(){
        return null;
        //return BUserWrapper.initWithModel(currentUserModel());
    }

    public User currentUserModel(){
        String authID = BNetworkManager.getAuthInterface().getCurrentUserAuthenticationId();
        if (StringUtils.isNotEmpty(authID))
        {
            User currentUser = (User) DaoCore.fetchEntityWithEntityID(BUser.class, authID);

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

    public Observable<Void> pushUser () {
        return null;
    }

    public void setUserOnline() {

    }

    public void goOffline() {

    }

    public void goOnline() {

    }

    public void observeUser(String entityID) {

    }

    public Observable<Thread> createThread (ArrayList<User> users, String name) {
        return null;
    }

    public Observable<Thread> createThread (ArrayList<User> users) {
        return null;
    }

    public Observable<User> addUsersToThread (ArrayList<User> users, Thread thread) {
        return null;
    }

    public Observable<User> removeUsersFromThread (ArrayList<User> users, Thread thread) {
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

    public Observable<Void> sendMessage (Message message) {
        return null;
    }

    public ArrayList<Message> messagesForThread (String threadID, boolean ascending)  {
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
