package sdk.chat.firestream.adapter;

import firestream.chat.namespace.Fire;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.ContactType;
import io.reactivex.Completable;
import sdk.chat.core.base.BaseContactHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.types.ConnectionType;

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
