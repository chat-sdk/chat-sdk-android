package wanderingdevelopment.tk.chatsdknetworkinterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.MeasureUnit;
import android.icu.util.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import wanderingdevelopment.tk.chatsdkcore.db.AuthCredentialDao;
import wanderingdevelopment.tk.chatsdkcore.db.DBAuthAdapter;
import wanderingdevelopment.tk.chatsdkcore.db.DaoCore;
import wanderingdevelopment.tk.chatsdkcore.entities.AuthCredential;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2016-11-06.
 */

public abstract class AuthManager {

    public enum Status {
        NONE(0),
        CONNECTED(2),
        AUTHENTICATED(4),
        DISCONNECTED(5),
        RECONNECTING(6),
        ERROR(7);

        private int value;
        Status(int value){
            this.value = value;
        }
    }

    protected Context ctx;
    protected DBAuthAdapter dbAuthAdapter;

    public AuthManager(Context ctx){
        this.ctx = ctx;
        this.dbAuthAdapter = new DBAuthAdapter(ctx);
    }

    public void subscribeToAuthStatus(final OnStatusChangedCallback callback) {
        getAuthStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Status>() {
            @Override
            public void accept(Status status) throws Exception {
                callback.onStatusChanged(status, "NONE");
            }
        }).subscribe();
    }

    protected abstract Observable<Status> getAuthStatusObservable();

    public abstract Status getStatus();

    public Single<Status> attemptLogin(final String userAlias, final String password){
        Single<Status> single;
        single = loginProcedure(userAlias, password);
        single.doOnSuccess(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                dbAuthAdapter.storeCredentials(userAlias, password);
            }
        });
        return single;
    }

    public void logout(){
        dbAuthAdapter.logout();
        logoutProcedure(); // must be last
    }

    public Single attemptRegister(final String userAlias, final String password){
        Single single;
        single = registerProcedure(userAlias, password);
        single.subscribeOn(Schedulers.single())
            .doOnSuccess(new Consumer() {
                @Override
                public void accept(Object o) throws Exception {
                    dbAuthAdapter.storeCredentials(userAlias, password);
                    attemptLogin(userAlias, password);
                }
            }).subscribe();
        return single;
    }

    public void attemptReconnect(){
        AuthCredential authCredential;
        if((authCredential = dbAuthAdapter.fetchCredentials()) != null){
            attemptLogin(authCredential.getUserAlias(),
                    authCredential.getUserPassword()).subscribe();
        }
    }

    public Observable<User> getCurrentUser(){
        return getCurrentUserProcedure();
    }

    public Single<Object> setCurrentUser(User user){
        return setCurrentUserProcedure(user);
    }

    protected abstract Single<Status> loginProcedure(String userAlias, String password);

    protected abstract Single registerProcedure(String userAlias, String password);

    protected abstract void logoutProcedure();

    protected abstract Observable<User> getCurrentUserProcedure();
    protected abstract Single<Object> setCurrentUserProcedure(User user);

    protected abstract void reconnectProcedure();

    public interface OnStatusChangedCallback{
        void onStatusChanged(Status status, String details);
    }

    class OnStatusChanged implements OnStatusChangedCallback{
        @Override
        public void onStatusChanged(Status status, String details) {
        }
    }

}
