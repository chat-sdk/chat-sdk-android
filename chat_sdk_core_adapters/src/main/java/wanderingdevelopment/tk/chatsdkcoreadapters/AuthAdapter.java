package wanderingdevelopment.tk.chatsdkcoreadapters;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import wanderingdevelopment.tk.chatsdkcore.AuthRemoteInterface;
import wanderingdevelopment.tk.chatsdkcore.ConnectionStatus;
import wanderingdevelopment.tk.chatsdkcore.db.AuthCredentialDao;
import wanderingdevelopment.tk.chatsdkcore.db.DaoCore;
import wanderingdevelopment.tk.chatsdkcore.entities.AuthCredential;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2017-01-10.
 */

public class AuthAdapter extends BaseAdapter {

    private static AuthAdapter authAdapter = null;
    private static AuthRemoteInterface remoteAdapter = null;

    public AuthAdapter(Context context, AuthRemoteInterface remoteInterface) {
        super(context);
        remoteAdapter = remoteInterface;
    }

    public static AuthAdapter initAuthAdapter(Context context, AuthRemoteInterface remoteInterface){
        authAdapter = new AuthAdapter(context, remoteInterface);
        return authAdapter;
    }

    public static AuthAdapter getAuthAdapter(){
        return authAdapter;
    }

    public void storeCredentials(String userAlias, String password){
        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor prefEditor;
        prefEditor = sharedPreferences.edit();
        prefEditor.putString(DaoCore.PREFERENCE_KEY_DATABASE_ID, userAlias);
        prefEditor.apply();

        DaoCore.getDaoCore(getContext(), userAlias);

        AuthCredential authCredential;
        authCredential = new AuthCredential();
        authCredential.setUserAlias(userAlias);
        authCredential.setUserPassword(password);
        getDaoCore().createEntity(authCredential);
    }

    public AuthCredential fetchCredentials(){
        AuthCredential authCredential;
        String userAlias;

        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());

        userAlias = sharedPreferences.getString(DaoCore.PREFERENCE_KEY_DATABASE_ID, null);

        if ( userAlias == null) return new AuthCredential();

        authCredential = getDaoCore().fetchEntityWithProperty(AuthCredential.class,
                AuthCredentialDao.Properties.UserAlias,
                userAlias);

        return authCredential;
    }


    public void logout(){
        DaoCore.resetDaoCore(getContext());
        removeCredentials();
        remoteAdapter.logout();// must happen after the default daocore is restored
    }
    private void removeCredentials() {
        SharedPreferences sharedPreferences = DaoCore.getSharedPreferences(getContext().getApplicationContext());
        SharedPreferences.Editor prefEditor;
        prefEditor = sharedPreferences.edit();
        prefEditor.putString(DaoCore.PREFERENCE_KEY_DATABASE_ID,
                DaoCore.PREFERENCE_VALUE_DEFAULT_EMPTY);
        prefEditor.apply();

        getDaoCore().deleteEntity(fetchCredentials());
    }

    public void subscribeToAuthStatus(final OnStatusChangedCallback callback) {
        if(remoteAdapter.subscribeToAuthStatus(callback)) {
            remoteAdapter.getAuthStatusObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(new Consumer<ConnectionStatus>() {
                        @Override
                        public void accept(ConnectionStatus status) throws Exception {
                            callback.onStatusChanged(status, "NONE");
                        }
                    }).subscribe();
        }
    }


    public Single<ConnectionStatus> attemptLogin(final String userAlias, final String password){
        Single<ConnectionStatus> single;
        single = remoteAdapter.login(userAlias, password);
        return single.doOnSuccess(new Consumer<ConnectionStatus>() {
            @Override
            public void accept(ConnectionStatus o) throws Exception {
                    storeCredentials(userAlias, password);
            }
        });
    }

    public Single attemptRegister(final String userAlias, final String password){
        Single single;
        single = remoteAdapter.register(userAlias, password);
        return single.subscribeOn(Schedulers.single())
                .doOnSuccess(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        storeCredentials(userAlias, password);
                        attemptLogin(userAlias, password);
                    }
                });
    }

    public void attemptReconnect(){
        AuthCredential authCredential;
        if((authCredential = fetchCredentials()) != null){
            attemptLogin(authCredential.getUserAlias(),
                    authCredential.getUserPassword()).subscribe();
        }
    }

    public Observable<User> getCurrentUser(){

        return remoteAdapter.getCurrentUser(fetchCredentials().getUserAlias());
    }

    public Single<User> setCurrentUser(User user){
        AuthCredential authCredential = authAdapter.fetchCredentials();
        user.setUserName(authCredential.getUserAlias());
        return remoteAdapter.setCurrentUser(user);
    }


    public interface OnStatusChangedCallback{
        void onStatusChanged(ConnectionStatus status, String details);
    }

    class OnStatusChanged implements OnStatusChangedCallback{
        @Override
        public void onStatusChanged(ConnectionStatus status, String details) {
        }
    }

}
