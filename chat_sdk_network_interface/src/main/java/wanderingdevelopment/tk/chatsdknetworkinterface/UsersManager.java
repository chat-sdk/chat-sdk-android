package wanderingdevelopment.tk.chatsdknetworkinterface;

import android.content.Context;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import wanderingdevelopment.tk.chatsdkcore.UsersInterface;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2016-10-23.
 */


public class UsersManager {
    private Context context;

    private UsersInterface remoteUsersAdapter;
    private UsersInterface dbUsersAdapter;

    public UsersManager(UsersInterface localInterface, UsersInterface remoteInterface){

        this.dbUsersAdapter = localInterface;
        this.remoteUsersAdapter = remoteInterface;
    }

    private Context getContext(){
        return context;
    }

    void getProfile(User user){
        //TODO: launch profile activity
    }

//    public Single<User> searchForUser(String string){
//        return dbUsersAdapter.getUser(string).takeUntil(remoteUsersAdapter.getUser(string)).;
//
//    }

    /**
     * Add user to DB, then add to remote.
     * If remote add fails, remove from local DB.
     * @param user
     * @return
     */
    public Flowable<User> addUser(final User user){
        return Flowable.concat
                (
                dbUsersAdapter.addOrUpdateUser(user).toFlowable(),
                remoteUsersAdapter.addOrUpdateUser(user).toFlowable()
                )
                .observeOn(Schedulers.single())
                .doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dbUsersAdapter.removeUser(user);
            }
        }).cache();
    }

    /**
     * Remove user from remote contacts, if successful, remove from local DB.
     * @param user
     * @return
     */
    public Flowable<User> removeUser(User user){
        return remoteUsersAdapter.removeUser(user).concatWith(
                dbUsersAdapter.removeUser(user));
    }

    /**
     * Get contact from local db, and update from remote
     * @param
     * @return
     */
//    public Flowable<User> refreshContact(User user){
//        return dbUsersAdapter.getUser(user.getUserName()).mergeWith(remoteUsersAdapter.getUser(user.getUserName()));
//    }


    public Observable<User> getContactList(){
        Observable<User> remoteContactList = remoteUsersAdapter.getAllAddedUsers();
        return Observable.merge
                (
                    dbUsersAdapter.getAllAddedUsers().takeUntil(remoteContactList),
                    remoteContactList
                ).cache();
    }
}
