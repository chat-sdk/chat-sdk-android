package sdk.chat.micro.group;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.micro.AbstractChat;
import sdk.chat.micro.firestore.FSPaths;
import sdk.chat.micro.message.Sendable;

public class GroupChat extends AbstractChat {

    protected String id;

    protected ArrayList<ListenerRegistration> listenerRegistrations = new ArrayList<>();

    public void connect() throws Exception {
        disposableList.add(messagesOn(FSPaths.groupChatMessagesRef(id)).subscribe(messageResult -> {

        }));
    }

    @Override
    public Single<String> send(String userId, Sendable sendable) {
        return null;
    }

    public void disconnect() {
        for (ListenerRegistration lr : listenerRegistrations) {
            lr.remove();
        }
        listenerRegistrations.clear();
    }

    public Completable addUser(String userId, RoleType roleType) {
        return addUserId(FSPaths.groupChatUsersRef(id), userId, roleType.get());
    }

    public Completable updateUser(String userId, RoleType roleType) {
        return updateUserId(FSPaths.groupChatUsersRef(id), userId, roleType.get());
    }

    public Completable removeUser(String userId) {
        return removeUserId(FSPaths.groupChatUsersRef(id), userId);
    }

    public Completable sendMessage() {

    }


}
