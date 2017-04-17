package wanderingdevelopment.tk.chatsdkcoreadapters;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.Subject;
import wanderingdevelopment.tk.chatsdkcore.UsersInterface;
import wanderingdevelopment.tk.chatsdkcore.db.UserDao;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2017-01-05.
 */

public class UsersAdapter extends BaseAdapter implements UsersInterface {

    private UsersInterface remoteUsersAdapter;
    private Subject<User> localUsersSearch = null;

    public UsersAdapter(Context context, UsersInterface remoteUsersAdapter){
        super(context);
        this.remoteUsersAdapter = remoteUsersAdapter;
    }

    @Override
    public List<String> getUserSearchFields() {
        return remoteUsersAdapter.getUserSearchFields();
    }

    private Consumer<User> getRemoteUserConsumer(){
        return new Consumer<User>() {
            @Override
            public void accept(User user) throws Exception {
                addOrUpdateUser(user).subscribe();
            }
        };
    }

    public Observable<User> getUser(final String userName){
        Observable<User> observable = Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                List<User> UserList = getDaoCore().fetchEntitiesWithProperty(User.class,
                        UserDao.Properties.UserName, userName);
                if(UserList.isEmpty()){
                    e.onError(new Throwable("No Users Found"));
                } else {
                    e.onNext((User) UserList.get(0));
                }
                e.onComplete();
            }
        }).concatWith(remoteUsersAdapter.getUser(userName)).onErrorResumeNext(remoteUsersAdapter.getUser(userName));

        return observable;
    }

    public Observable<User> searchUser(final String searchString, final String field){
        Observable<User> observable = Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                List<User> userList;
                userList = getDaoCore().fetchEntitiesWithPropertyLike(User.class, searchString);
                for ( User user : userList ) {
                    e.onNext(user);
                }
                e.onComplete();
            }
        });

        return observable.cache();
    }

    public Observable<User> searchUserRemote(final String searchString, String field){
        final List<User> localUsers = new ArrayList<>();
        localUsers.clear();
        return this.remoteUsersAdapter.searchUser(searchString, field)
                .filter(new Predicate<User>() {
            @Override
            public boolean test(User user) throws Exception {
                for (User localUser : localUsers) {
                    if (localUser.getUserName().equals(user.getUserName())){
                        return false;
                    }
                }
                localUsers.add(user);
                return true;
            }
        }).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<User> getAllAddedUsers(){
        return Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                List<User> users = getDaoCore().fetchEntitiesOfClass(User.class);
                User currentUser = (User) getDaoCore().fetchCurrentUser();
                users.remove(currentUser);
                for (User user: users) {
                    if(!user.getUserName().equals(currentUser.getUserName())) {
                        e.onNext(user);
                    }
                }
                e.onComplete();
            }
        }).concatWith(this.remoteUsersAdapter.getAllAddedUsers());
    }

    public Single<User> removeUser(final String userName){
        return getUser(userName)
                .first(new User())
                .flatMap(new Function<User, SingleSource<User>>() {
            @Override
            public SingleSource<User> apply(User user) throws Exception {
                return removeUser(user);
            }
        }).cache();
    }

    public Single<User> removeUser(final User user){
        return new Single<User>() {
            @Override
            protected void subscribeActual(SingleObserver<? super User> observer) {
                getDaoCore().deleteEntity(user);
            }
        };
    }

    public Single<User> addOrUpdateUser(final User user){
        return new Single<User>() {
            @Override
            protected void subscribeActual(SingleObserver<? super User> observer) {
                getDaoCore().createOrReplace(user);
                if(localUsersSearch != null)
                    localUsersSearch.onNext(user);
            }
        };
    }

    @Override
    public Observable<User> getUserAvailabilityObservable() {
        return remoteUsersAdapter.getUserAvailabilityObservable().doOnNext(new Consumer<User>() {
            @Override
            public void accept(User user) throws Exception {
                addOrUpdateUser(user);
            }
        });
    }
}
