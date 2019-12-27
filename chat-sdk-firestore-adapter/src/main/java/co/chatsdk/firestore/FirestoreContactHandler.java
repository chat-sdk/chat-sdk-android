package co.chatsdk.firestore;

import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.namespace.FireflyUser;
import firefly.sdk.chat.types.ContactType;

public class FirestoreContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(User user, ConnectionType type) {
        FireflyUser fireflyUser = new FireflyUser(user.getEntityID());
        return Fl.y.addContact(fireflyUser, ContactType.contact());
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        FireflyUser fireflyUser = new FireflyUser(user.getEntityID());
        return Fl.y.removeContact(fireflyUser);
    }

}
