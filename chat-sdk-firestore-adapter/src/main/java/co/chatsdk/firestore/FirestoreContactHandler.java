package co.chatsdk.firestore;

import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.ConnectionType;
import io.reactivex.Completable;
import sdk.chat.micro.Fireflyy;
import sdk.chat.micro.namespace.MicroUser;
import sdk.chat.micro.types.ContactType;

public class FirestoreContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(User user, ConnectionType type) {
        MicroUser microUser = new MicroUser(user.getEntityID());
        return Fireflyy.shared().addContact(microUser, ContactType.contact());
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        MicroUser microUser = new MicroUser(user.getEntityID());
        return Fireflyy.shared().removeContact(microUser);
    }

}
