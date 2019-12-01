package sdk.chat.micro.group;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.micro.AbstractChat;
import sdk.chat.micro.firestore.FSPaths;
import sdk.chat.micro.message.Sendable;

public class GroupChat extends AbstractChat {

    protected String id;
    protected Date loadMessagesFrom;

    protected HashMap<String, String> userRoles;
    protected Listener userRoleChangedListener;

    public GroupChat(String id, Date loadMessagesFrom) {
        this.id = id;
        this.loadMessagesFrom = loadMessagesFrom;
    }

    public void connect() throws Exception {

        disposableList.add(messagesOn(FSPaths.groupChatMessagesRef(id), loadMessagesFrom).subscribe(this::passMessageResultToStream));

        disposableList.add(userListOn(FSPaths.groupChatUsersRef(id)).subscribe(map -> {
            userRoles.clear();
            userRoles.putAll(map);
            if (userRoleChangedListener != null) {
                userRoleChangedListener.onEvent();
            }
        }, this));

    }

    @Override
    public Single<String> send(String userId, Sendable sendable) {
        return send(FSPaths.groupChatMessagesRef(id), sendable);
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

}
