package co.chatsdk.firestore;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.base.BaseContactHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.events.EventType;
import sdk.chat.micro.events.UserEvent;
import sdk.chat.micro.namespace.MicroUser;
import sdk.chat.micro.types.ContactType;

public class FirestoreContactHandler extends BaseContactHandler {

    @Override
    public Completable addContact(User user, ConnectionType type) {
        MicroUser microUser = new MicroUser(user.getEntityID());
        return MicroChatSDK.shared().addContact(microUser, ContactType.contact());
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        MicroUser microUser = new MicroUser(user.getEntityID());
        return MicroChatSDK.shared().removeContact(microUser);
    }

}
