package wanderingdevelopment.tk.chatsdkcore;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import wanderingdevelopment.tk.chatsdkcore.entities.User;

/**
 * Created by kykrueger on 2017-01-09.
 */

public interface UsersInterface {

    /** getUserSearchFields();
     * Provides all types of searching available for the remote.
     * @return fields available for searching a user in the database.
     */
    List<String> getUserSearchFields();

    Observable<User> getUser(final String userName);

    Observable<User> getAllAddedUsers();

    Observable<User> searchUser(final String key, final String field);

    Single<User> removeUser(final String userName);

    Single<User> removeUser(final User user);

    Single<User> addOrUpdateUser(final User user);

    Observable<User> getUserAvailabilityObservable();

}
