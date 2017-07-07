package co.chatsdk.core.handlers;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ContactHandler {

    /**
    * @brief Get a list of the user's contacts
    */
    List<BUser> contacts();

    /**
    * @brief Get a list of the user's contacts
    */
    List<BUser> contactsWithType (ConnectionType type);

    /**
    * @brief Add a user to contacts
    */
    Completable addContact (BUser user, ConnectionType type);
    Completable deleteContact (BUser user, ConnectionType type);
}
