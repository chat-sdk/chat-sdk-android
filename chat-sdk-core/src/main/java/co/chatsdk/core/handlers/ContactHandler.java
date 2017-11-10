package co.chatsdk.core.handlers;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface ContactHandler {

    /**
    * Get a list of the user's contacts
    */
    List<User> contacts();

    /**
    * Get a list of the user's contacts
    */
    List<User> contactsWithType (ConnectionType type);

    /**
    * Add a user to contacts
    */
    Completable addContact (User user, ConnectionType type);
    Completable deleteContact (User user, ConnectionType type);

    Completable addContacts(ArrayList<User> users, ConnectionType type);
    Completable deleteContacts(ArrayList<User> users, ConnectionType type);
}
