package co.chatsdk.firestream;

import sdk.chat.core.base.BaseContactHandler;
import sdk.chat.core.dao.User;
import sdk.chat.core.types.ConnectionType;
import firestream.chat.namespace.Fire;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.pro.types.ContactType;
import io.reactivex.Completable;

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
