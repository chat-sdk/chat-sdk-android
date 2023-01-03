package sdk.chat.core.base;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.User;
import sdk.chat.core.handlers.ContactHandler;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import io.reactivex.Completable;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public class BaseContactHandler implements ContactHandler {

    @Override
    public List<User> contacts() {
        if (ChatSDK.currentUser() != null) {
            return ChatSDK.currentUser().getContacts();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean exists(User user) {
        for (User u : contacts()) {
            if (u.equalsEntity(user)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> contactsWithType(ConnectionType type) {
        if (ChatSDK.currentUser() != null) {
            return ChatSDK.currentUser().getContacts(type);
        }
        return new ArrayList<>();
    }

    @Override
    public Completable addContact(User user, ConnectionType type) {
        return addContactLocal(user, type);
    }

    @Override
    public Completable addContactLocal(User user, ConnectionType type) {
        return ChatSDK.core().userOn(user).doOnComplete(() -> {
            if (ChatSDK.currentUser() != null && !user.isMe()) {

                ChatSDK.hook().executeHook(HookEvent.ContactWillBeAdded, HookEvent.userData(user));

                ChatSDK.currentUser().addContact(user, type);

                ChatSDK.hook().executeHook(HookEvent.ContactWasAdded, HookEvent.userData(user));
            }
        });
    }

    @Override
    public void deleteContactLocal(User user, ConnectionType type) {
        if (ChatSDK.currentUser() != null && !user.isMe()) {

            ChatSDK.hook().executeHook(HookEvent.ContactWillBeDeleted, HookEvent.userData(user));

            ChatSDK.currentUser().deleteContact(user, type);

            ChatSDK.hook().executeHook(HookEvent.ContactWasDeleted, HookEvent.userData(user));

            ChatSDK.core().userOff(user);
        }
    }

    @Override
    public Completable deleteContact(User user, ConnectionType type) {
        deleteContactLocal(user, type);
        return Completable.complete();
    }

    @Override
    public Completable addContacts(ArrayList<User> users, ConnectionType type) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (User user : users) {
            completables.add(addContact(user, type));
        }
        return Completable.concat(completables);
    }

    @Override
    public Completable deleteContacts(ArrayList<User> users, ConnectionType type) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (User user : users) {
            completables.add(deleteContact(user, type));
        }
        return Completable.concat(completables);
    }

}
