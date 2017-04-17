package wanderingdevelopment.tk.chatsdkcore;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observables.ConnectableObservable;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2017-02-20.
 */

public interface AuthRemoteInterface {

    Single<ConnectionStatus> login(String userAlias, String password);

    Single register(String userAlias, String password);

    void logout();

    Observable<User> getCurrentUser(String userAlias);
    Single<User> setCurrentUser(User user);

    void reconnectProcedure();


    Observable<ConnectionStatus> getAuthStatusObservable();
    Boolean subscribeToAuthStatus(Object callback);


    ConnectionStatus getStatus();
}
