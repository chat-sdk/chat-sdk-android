package wanderingdevelopment.tk.chatsdkfirebasenetworkimplementation;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import timber.log.Timber;
import wanderingdevelopment.tk.chatsdkcore.BuildConfig;
import wanderingdevelopment.tk.chatsdkcore.db.DaoCore;
import wanderingdevelopment.tk.chatsdkcore.entities.User;
import wanderingdevelopment.tk.chatsdknetworkinterface.AuthManager;

/**
 * Created by KyleKrueger on 26.03.2017.
 */

public class FirebaseAuthManager extends AuthManager {

    private FirebaseUser firebaseUser = null;

    public FirebaseAuthManager (Context context){
        super(context);
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    protected Observable<Status> getAuthStatusObservable() {
        return null;
    }

    @Override
    protected Single<Object> setCurrentUserProcedure(User user) {
        return null;
    }

    @Override
    protected Observable<User> getCurrentUserProcedure() {
        return null;
    }

    @Override
    protected Single<Status> loginProcedure(String userAlias, String password) {
        return null;
    }

    @Override
    protected void logoutProcedure() {

    }

    @Override
    protected Single registerProcedure(String userAlias, String password) {
        return null;
    }

    @Override
    protected void reconnectProcedure() {

    }


    protected class LoginCompletedConsumer implements Consumer<FirebaseUser> {
        @Override
        public void accept(FirebaseUser firebaseUser) throws Exception {
            if (BuildConfig.DEBUG) Timber.v("onLoginCompleted");


        }
    }


}
