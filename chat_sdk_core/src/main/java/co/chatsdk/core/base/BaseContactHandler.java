package co.chatsdk.core.base;

import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.ContactHandler;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class BaseContactHandler implements ContactHandler {

    @Override
    public List<BUser> contacts() {
        return NM.currentUser().getContacts();
    }

    @Override
    public List<BUser> contactsWithType(ConnectionType type) {
        return NM.currentUser().getContacts(type);
    }

    @Override
    public Completable addContact(BUser user, ConnectionType type) {
        NM.currentUser().addContact(user, type);
        return Completable.complete();
    }

    @Override
    public Completable deleteContact(BUser user, ConnectionType type) {
        NM.currentUser().deleteContact(user, type);
        return Completable.complete();
    }
}
