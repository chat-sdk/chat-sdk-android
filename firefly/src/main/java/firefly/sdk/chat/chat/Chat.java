package firefly.sdk.chat.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import firefly.sdk.chat.events.EventType;
import firefly.sdk.chat.events.UserEvent;
import firefly.sdk.chat.filter.MessageStreamFilter;
import firefly.sdk.chat.firebase.rx.MultiQueueSubject;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.firebase.service.Paths;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import firefly.sdk.chat.message.DeliveryReceipt;
import firefly.sdk.chat.message.Message;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.message.TextMessage;
import firefly.sdk.chat.message.TypingState;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.namespace.FireflyUser;
import firefly.sdk.chat.types.DeliveryReceiptType;
import firefly.sdk.chat.types.InvitationType;
import firefly.sdk.chat.types.RoleType;
import firefly.sdk.chat.types.TypingStateType;

public class Chat extends AbstractChat {

    public static class Meta {
        public String name;
        public String avatarURL;
        public Date created;
    }

    protected String id;
    protected Date joined;
    protected Date created;
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

        System.out.println("Connect to chat: " + id);

        // If delivery receipts are enabled, send the delivery receipt
        if (config.deliveryReceiptsEnabled) {
            dl.add(getEvents()
                    .getMessages()
                    .pastAndNewEvents()
                    .filter(MessageStreamFilter.notFromMe())
                    .flatMapSingle(message -> sendDeliveryReceipt(DeliveryReceiptType.received(), message.id))
                    .doOnError(this)
                    .subscribe());
        }

        dl.add(listChangeOn(Paths.groupChatUsersPath(id)).subscribe(listEvent -> {
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
        dl.add(Fl.y.getFirebaseService().chat
                .metaOn(Paths.groupChatMetaPath(id))
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meta -> {
            if (meta != null) {
                String newName = meta.name;
                if (newName != null && !newName.equals(name)) {
                    name = newName;
                    nameStream.onNext(name);
                }
                String newAvatarURL = meta.avatarURL;
                if (newAvatarURL != null && !newAvatarURL.equals(avatarURL)) {
                    avatarURL = newAvatarURL;
                    avatarURLStream.onNext(avatarURL);
                }
                if (meta.created != null) {
                    created = meta.created;
                }
            }
        }, this));

        super.connect();
    }

    @Override
    protected Path messagesPath() {
        return Paths.groupChatMessagesPath(id);
    }

    public static Single<Chat> create(String name, String avatarURL, List<User> users) {
        return Single.create((SingleOnSubscribe<HashMap<String, Object>>) emitter -> {
            HashMap<String, Object> meta = new HashMap<>();

            meta.put(Keys.Created, Fl.y.getFirebaseService().core.timestamp());
            if (name != null) {
                meta.put(Keys.Name, name);
            }
            if (avatarURL != null) {
                meta.put(Keys.Avatar, avatarURL);
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put(Paths.Meta, meta);

            emitter.onSuccess(data);
        }).flatMap(data -> Fl.y.getFirebaseService().chat.add(Paths.chatsPath(), data).map(chatId -> {
            return new Chat(chatId, null);
        })).flatMap(groupChat -> {
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
                completables.add(Fl.y.sendInvitation(user.id, InvitationType.chat(), id).ignoreElement());
            }
        }
        return Completable.merge(completables).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<String> send(String toUserId, Sendable sendable) {
        return send(Paths.groupChatMessagesPath(id), sendable);
    }

    public Completable addUser(User user) {
        return addUser(Paths.groupChatUsersPath(id), null, user);
    }

    public Completable addUsers(User... users) {
        return addUsers(Paths.groupChatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable addUsers(List<User> users) {
        return addUsers(Paths.groupChatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable updateUser(User user) {
        return updateUser(Paths.groupChatUsersPath(id), User.roleTypeDataProvider(), user);
    }

    public Completable updateUsers(List<User> users) {
        return updateUsers(Paths.groupChatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable updateUsers(User... users) {
        return updateUsers(Paths.groupChatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable removeUser(User user) {
        return removeUser(Paths.groupChatUsersPath(id), user);
    }

    public Completable removeUsers(User... user) {
        return removeUsers(Paths.groupChatUsersPath(id), user);
    }

    public Completable removeUsers(List<User> user) {
        return removeUsers(Paths.groupChatUsersPath(id), user);
    }

    public String getId() {
        return id;
    }

    public Single<String> send(Sendable sendable) {
        return this.send(Paths.groupChatMessagesPath(id), sendable);
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

    /**
     * Update the role for a user - whether you can do this will
     * depend childOn your admin level
     * @param user to change role
     * @param roleType new role
     * @return completion
     */
    public Completable setRole(User user, RoleType roleType) {
        user.roleType = roleType;
        return updateUser(user);
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

    public ArrayList<FireflyUser> getFireflyUsers() {
        ArrayList<FireflyUser> fireflyUsers = new ArrayList<>();
        for (User u : users) {
            fireflyUsers.add(FireflyUser.fromUser(u));
        }
        return fireflyUsers;
    }

}
