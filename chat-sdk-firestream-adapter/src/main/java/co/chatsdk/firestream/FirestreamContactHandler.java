package co.chatsdk.firestream;

import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.ContactType;

public class FirestreamContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(User user, ConnectionType type) {
        FireStreamUser firestreamUser = new FireStreamUser(user.getEntityID());
        return Fire.stream().addContact(firestreamUser, ContactType.contact());
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        FireStreamUser firestreamUser = new FireStreamUser(user.getEntityID());
        return Fire.stream().removeContact(firestreamUser);
    }

}
