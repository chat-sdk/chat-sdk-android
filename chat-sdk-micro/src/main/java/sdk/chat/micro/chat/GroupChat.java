package sdk.chat.micro.chat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.Paths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TextMessage;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.RoleType;
import sdk.chat.micro.types.TypingStateType;

public class GroupChat extends AbstractChat {

    public static class User extends ListUser {
        public User(String id, RoleType role) {
            this.id = id;
            this.value = role.get();
        }

        public RoleType roleType() {
            return new RoleType(value);
        }
    }

    protected String id;
    protected Date loadMessagesFrom;

    protected HashMap<String, RoleType> userRoles = new HashMap<>();
    protected Listener userRoleChangedListener;

    public GroupChat(String id) {
        this.id = id;
    }

    public GroupChat(String id, Date loadMessagesFrom) {
        this(id);
        this.loadMessagesFrom = loadMessagesFrom;
    }

    public void connect() throws Exception {
        super.connect();

        // If delivery receipts are enabled, send the delivery receipt
        if (config.deliveryReceiptsEnabled) {
            dl.add(messageStream.flatMapSingle(message -> sendDeliveryReceipt(DeliveryReceiptType.received(), message.id))
                    .doOnError(this)
                    .subscribe());
        }

        dl.add(listOn(Paths.groupChatUsersRef(id)).subscribe(map -> {
            userRoles.clear();
            for (String userId: map.keySet()) {
                HashMap<String, String> userRole = map.get(userId);
                if (userRole != null) {
                    userRoles.put(userId, new RoleType(userRole.get(Keys.Role)));
                }
            }
            if (userRoleChangedListener != null) {
                userRoleChangedListener.onEvent();
            }
        }));
    }

    @Override
    protected CollectionReference messagesRef() {
        return Paths.groupChatMessagesRef(id);
    }

    public static Single<GroupChat> create(String name, String avatarURL, List<GroupChat.User> users) {
        return Single.create((SingleOnSubscribe<GroupChat>) emitter -> {

            HashMap<String, Object> meta = new HashMap<>();

            meta.put(Keys.Created, FieldValue.serverTimestamp());
            if (name != null) {
                meta.put(Keys.Name, name);
            }
            if (avatarURL != null) {
                meta.put(Keys.Avatar, avatarURL);
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put(Paths.Meta, meta);

            Paths.groupChatsRef().add(data).addOnSuccessListener(documentReference -> {
                System.out.println("");
                emitter.onSuccess(new GroupChat(documentReference.getId(), null));

            }).addOnFailureListener(emitter::onError);
        }).flatMap(groupChat -> {
            ArrayList<GroupChat.User> usersToAdd = new ArrayList<>();

            String currentUserId = MicroChatSDK.shared().currentUserId();
            for (GroupChat.User user : users) {
                if (!user.id.equals(currentUserId)) {
                    usersToAdd.add(user);
                }
            }
            usersToAdd.add(new GroupChat.User(MicroChatSDK.shared().currentUserId(), RoleType.owner()));

            return groupChat.addUsers(usersToAdd)
                    .andThen(groupChat.inviteUsers(users))
                    .toSingle(() -> groupChat);
        });
    }

    public Completable inviteUsers(List<GroupChat.User> users) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (GroupChat.User user : users) {
            if (!user.id.equals(MicroChatSDK.shared().currentUserId())) {
                completables.add(MicroChatSDK.shared().sendInvitation(user.id, InvitationType.group(), id).ignoreElement());
            }
        }
        return Completable.merge(completables);
    }

    @Override
    public Single<String> send(String userId, Sendable sendable) {
        return send(Paths.groupChatMessagesRef(id), sendable);
    }

    public Completable addUser(String userId, RoleType roleType) {
        return addUserId(Paths.groupChatUsersRef(id), userId, roleType.get());
    }

    public Completable addUser(User user) {
        return addUser(user.id, user.roleType());
    }

    public Completable updateUser(User user) {
        return updateUser(user.id, user.roleType());
    }

    public Completable updateUser(String userId, RoleType roleType) {
        return updateUserId(Paths.groupChatUsersRef(id), userId, roleType.get());
    }

    public Completable removeUser(String userId) {
        return removeUserId(Paths.groupChatUsersRef(id), userId);
    }

    public Completable addUsers(List<GroupChat.User> users) {
        return addUserIds(Paths.groupChatUsersRef(id), new ArrayList<>(users));
    }

    public Completable updateUsers(List<GroupChat.User> users) {
        return updateUserIds(Paths.groupChatUsersRef(id), new ArrayList<>(users));
    }

    public Completable removeUsers(List<String> userIds) {
        return removeUserIds(Paths.groupChatUsersRef(id), userIds);
    }

    public String getId() {
        return id;
    }

    public Single<String> send(Sendable sendable) {
        return this.send(Paths.groupChatMessagesRef(id), sendable);
    }

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message is delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Single<String> sendDeliveryReceipt(DeliveryReceiptType type, String messageId) {
        return send(new DeliveryReceipt(type, messageId));
    }

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Single<String> sendTypingIndicator(TypingStateType type) {
        return send(new TypingState(type));
    }

    public Single<String> sendMessageWithText(String text) {
        return send(new TextMessage(text));
    }

    public Single<String> sendMessageWithBody(HashMap<String, Object> body) {
        return send(new Message(body));
    }

    public RoleType getRoleTypeForUser(String userId) {
        return userRoles.get(userId);
    }

    public List<String> getUserIdsForRoleType(RoleType roleType) {
        ArrayList<String> userIds = new ArrayList<>();
        for (String userId: userRoles.keySet()) {
            if (userRoles.get(userId).equals(roleType)) {
                userIds.add(userId);
            }
        }
        return userIds;
    }

}
