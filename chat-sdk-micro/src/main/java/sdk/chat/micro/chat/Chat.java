package sdk.chat.micro.chat;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.User;
import sdk.chat.micro.events.EventType;
import sdk.chat.micro.events.UserEvent;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.Paths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TextMessage;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.namespace.MicroUser;
import sdk.chat.micro.rx.MultiQueueSubject;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.RoleType;
import sdk.chat.micro.types.TypingStateType;

public class Chat extends AbstractChat {

    protected String id;
    protected Date joined;
    protected String name;
    protected String avatarURL;

    protected ArrayList<User> users = new ArrayList<>();
    protected MultiQueueSubject<UserEvent> userEvents = MultiQueueSubject.create();

    protected BehaviorSubject<String> nameStream = BehaviorSubject.create();
    protected BehaviorSubject<String> avatarURLStream = BehaviorSubject.create();

    public Chat(String id) {
        this.id = id;
    }

    public Chat(String id, Date joined) {
        this(id);
        this.joined = joined;
    }

    public void connect() throws Exception {
        disconnect();

        // If delivery receipts are enabled, send the delivery receipt
        if (config.deliveryReceiptsEnabled) {
            dl.add(getStream().getMessages().pastAndNewEvents().flatMapSingle(message -> sendDeliveryReceipt(DeliveryReceiptType.received(), message.id))
                    .doOnError(this)
                    .subscribe());
        }

        dl.add(listChangeOn(Paths.groupChatUsersRef(id)).subscribe(listEvent -> {
            UserEvent userEvent = UserEvent.from(listEvent);
            if (userEvent.type == EventType.Added) {
                users.add(userEvent.user);
            }
            if (userEvent.type == EventType.Removed) {
                users.remove(userEvent.user);
            }
            userEvents.onNext(userEvent);
        }));

        // Handle name and image change
        Paths.groupChatRef(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    stream.impl_throwablePublishSubject().onNext(e);
                }
                else if (snapshot != null && snapshot.exists()) {
                    HashMap<String, Object> meta = (HashMap<String, Object>) snapshot.getData().get(Keys.Meta);
                    if (meta != null) {
                        Object nameObject = meta.get(Keys.Name);
                        if (nameObject instanceof String) {
                            name = (String) nameObject;
                        }
                        Object avatarURLObject = meta.get(Keys.Avatar);
                        if (avatarURLObject instanceof String) {
                            avatarURL = (String) avatarURLObject;
                        }
                    }
                }
            }
        });

        super.connect();
    }

    @Override
    protected CollectionReference messagesRef() {
        return Paths.groupChatMessagesRef(id);
    }

    public static Single<Chat> create(String name, String avatarURL, List<User> users) {
        return Single.create((SingleOnSubscribe<Chat>) emitter -> {

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
                emitter.onSuccess(new Chat(documentReference.getId(), null));

            }).addOnFailureListener(emitter::onError);
        }).flatMap(groupChat -> {
            ArrayList<User> usersToAdd = new ArrayList<>();

            for (User user : users) {
                if (!user.isMe()) {
                    usersToAdd.add(user);
                }
            }
            usersToAdd.add(User.currentUser(RoleType.owner()));

            return groupChat.addUsers(usersToAdd)
                    .andThen(groupChat.inviteUsers(users))
                    .toSingle(() -> groupChat);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable inviteUsers(List<User> users) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (User user : users) {
            if (!user.isMe()) {
                completables.add(MicroChatSDK.shared().sendInvitation(user.id, InvitationType.chat(), id).ignoreElement());
            }
        }
        return Completable.merge(completables);
    }

    @Override
    public Single<String> send(String toUserId, Sendable sendable) {
        return send(Paths.groupChatMessagesRef(id), sendable);
    }

    public Completable addUser(User user) {
        return addUser(Paths.groupChatUsersRef(id), null, user);
    }

    public Completable addUsers(User... users) {
        return addUsers(Paths.groupChatUsersRef(id), User.roleTypeDataProvider(), users);
    }

    public Completable addUsers(List<User> users) {
        return addUsers(Paths.groupChatUsersRef(id), User.roleTypeDataProvider(), users);
    }

    public Completable updateUser(User user) {
        return updateUser(Paths.groupChatUsersRef(id), User.roleTypeDataProvider(), user);
    }

    public Completable updateUsers(List<User> users) {
        return updateUsers(Paths.groupChatUsersRef(id), User.roleTypeDataProvider(), users);
    }

    public Completable updateUsers(User... users) {
        return updateUsers(Paths.groupChatUsersRef(id), User.roleTypeDataProvider(), users);
    }

    public Completable removeUser(User user) {
        return removeUser(Paths.groupChatUsersRef(id), user);
    }

    public Completable removeUsers(User... user) {
        return removeUsers(Paths.groupChatUsersRef(id), user);
    }

    public Completable removeUsers(List<User> user) {
        return removeUsers(Paths.groupChatUsersRef(id), user);
    }

    public String getId() {
        return id;
    }

    public Single<String> send(Sendable sendable) {
        return this.send(Paths.groupChatMessagesRef(id), sendable);
    }

    public Completable leave() {
        return removeUser(User.currentUser()).doOnComplete(this::disconnect);
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
        for (User user: users) {
            if (user.id.equals(userId)) {
                return user.roleType;
            }
        }
        return null;
    }

    public List<User> getUsersForRoleType(RoleType roleType) {
        ArrayList<User> result = new ArrayList<>();
        for (User user: users) {
            if (user.roleType.equals(roleType)) {
                result.add(user);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object chat) {
        if (chat instanceof Chat) {
            return id.equals(((Chat) chat).id);
        }
        return false;
    }

    public Observable<UserEvent> getUserEventStream() {
        return userEvents.hide();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public Observable<String> getNameStream() {
        return nameStream.hide();
    }

    public Observable<String> getAvatarURLStream() {
        return avatarURLStream.hide();
    }

    public ArrayList<MicroUser> getMicroUsers() {
        ArrayList<MicroUser> microUsers = new ArrayList<>();
        for (User u : users) {
            microUsers.add(MicroUser.fromUser(u));
        }
        return microUsers;
    }

}
