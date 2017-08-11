package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.User;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface SearchHandler {

    public Observable<User> usersForIndex(final String index, final String value);
    /**
     * @brief Methods to handle search
     */
    //-(RXPromise *) updateIndexForUser: (id<PUser>) userModel;

    /**
     * @brief Get users for a given index i.e. name, email with the value...
     */
//    -(RXPromise *) usersForIndexes: (NSArray *) indexes withValue: (NSString *) value limit: (int) limit userAdded: (void(^)(id<PUser> user)) userAdded;

//    -(RXPromise *) availableIndexes;
}
