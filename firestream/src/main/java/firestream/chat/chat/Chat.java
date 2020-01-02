package firestream.chat.chat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import firestream.chat.events.EventType;
import firestream.chat.events.UserEvent;
import firestream.chat.filter.MessageStreamFilter;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.Paths;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.message.TypingState;

import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.InvitationType;
import firestream.chat.types.RoleType;
import firestream.chat.types.TypingStateType;

public class Chat extends AbstractChat {

    public static class Meta {
        public String name = "";
        public String imageURL = "";
        public Date created;

        public Meta() {
        }

        public Meta(String name, String imageURL) {
            this(name, imageURL, null);
        }

        public Meta(String name, String imageURL, Date created) {
            this.name = name;
            this.imageURL = imageURL;
            this.created = created;
        }

        public HashMap<String, Object> toData(boolean includeTimestamp) {
            HashMap<String, Object> data = new HashMap<>();

            data.put(Keys.Name, name);
            data.put(Keys.ImageURL, imageURL);

            if (includeTimestamp) {
                data.put(Keys.Created, Fire.Stream.getFirebaseService().core.timestamp());
            }

            HashMap<String, Object> meta = new HashMap<>();
            meta.put(Keys.Meta, data);

            return meta;
        }

        public Meta copy() {
            Meta meta = new Meta(name, imageURL);
            meta.created = created;
            return meta;
        }

        public static Meta with(String name, String imageURL) {
            return new Meta(name, imageURL);
        }

    }

    protected String id;
    protected Date joined;
    protected Meta meta = new Meta();

    protected ArrayList<User> users = new ArrayList<>();
    protected MultiQueueSubject<UserEvent> userEvents = MultiQueueSubject.create();

    protected BehaviorSubject<String> nameStream = BehaviorSubject.create();
    protected BehaviorSubject<String> imageURLStream = BehaviorSubject.create();

    public Chat(String id) {
        this.id = id;
    }

    public Chat(String id, Date joined, Meta meta) {
        this(id, joined);
        this.meta = meta;
    }

    public Chat(String id, Date joined) {
        this(id);
        this.joined = joined;
    }

    public void connect() throws Exception {

        System.out.println("Connect to chat: " + id);

        // If delivery receipts are enabled, send the delivery receipt
        if (config.deliveryReceiptsEnabled) {
            dm.add(getEvents()
                    .getMessages()
                    .pastAndNewEvents()
                    .filter(MessageStreamFilter.notFromMe())
                    .flatMapCompletable(this::markReceived)
                    .doOnError(this)
                    .subscribe());
        }

        dm.add(listChangeOn(Paths.chatUsersPath(id)).subscribe(listEvent -> {
            UserEvent userEvent = UserEvent.from(listEvent);
            User user = userEvent.user;

            // If we start by removing the user. If it is a remove event
            // we leave it at that. Otherwise we add that user back in
            users.remove(user);
            if (userEvent.type != EventType.Removed) {
                users.add(user);
            }

            userEvents.onNext(userEvent);
        }));

        // Handle name and image change
        dm.add(Fire.Stream.getFirebaseService().chat
                .metaOn(path())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newMeta -> {
            if (newMeta != null) {

                if (newMeta.name != null && !newMeta.name.equals(meta.name)) {
                    meta.name = newMeta.name;
                    nameStream.onNext(meta.name);
                }
                if (newMeta.imageURL != null && !newMeta.imageURL.equals(meta.imageURL)) {
                    meta.imageURL = newMeta.imageURL;
                    imageURLStream.onNext(meta.imageURL);
                }
                if (newMeta.created != null) {
                    meta.created = newMeta.created;
                }
            }
        }, this));

        super.connect();
    }

    @Override
    protected Path messagesPath() {
        return Paths.chatMessagesPath(id);
    }

    public Completable setName(String name) {
        if (this.meta.name.equals(name)) {
            return Completable.complete();
        } else {
            Meta newMeta = meta.copy();
            newMeta.name = name;
            return Fire.Stream.getFirebaseService().chat.updateMeta(path(), newMeta.toData(false)).doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    meta.name = name;
                }
            });
        }
    }

    public Completable setImageURL(String url) {
        if (this.meta.imageURL.equals(url)) {
            return Completable.complete();
        } else {
            Meta newMeta = meta.copy();
            newMeta.imageURL = url;
            return Fire.Stream.getFirebaseService().chat.updateMeta(path(), newMeta.toData(false));
        }
    }
    public static Single<Chat> create(final String name, final String imageURL, final List<User> users) {
        return Fire.Stream.getFirebaseService().chat.add(Paths.chatsPath(), Meta.with(name, imageURL).toData(true)).flatMap(chatId -> {
            Chat chat = new Chat(chatId, null, new Meta(name, imageURL));

            ArrayList<User> usersToAdd = new ArrayList<>(users);

            // Make sure the current user is the owner
            usersToAdd.remove(User.currentUser());
            usersToAdd.add(User.currentUser(RoleType.owner()));

            return chat.addUsers(usersToAdd)
                    .andThen(chat.inviteUsers(users))
                    .toSingle(() -> chat);

        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable inviteUsers(List<User> users) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (User user : users) {
            if (!user.isMe()) {
                completables.add(Fire.Stream.sendInvitation(user.id, InvitationType.chat(), id));
            }
        }
        return Completable.merge(completables).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addUser(User user) {
        return addUser(Paths.chatUsersPath(id), null, user);
    }

    public Completable addUsers(User... users) {
        return addUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable addUsers(List<User> users) {
        return addUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users).doOnComplete(() -> {
            this.users.addAll(users);
        });
    }

    public Completable updateUser(User user) {
        return updateUser(Paths.chatUsersPath(id), User.roleTypeDataProvider(), user);
    }

    public Completable updateUsers(List<User> users) {
        return updateUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable updateUsers(User... users) {
        return updateUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    public Completable removeUser(User user) {
        return removeUser(Paths.chatUsersPath(id), user);
    }

    public Completable removeUsers(User... user) {
        return removeUsers(Paths.chatUsersPath(id), user);
    }

    public Completable removeUsers(List<User> user) {
        return removeUsers(Paths.chatUsersPath(id), user);
    }

    public String getId() {
        return id;
    }

    public Completable send(Sendable sendable) {
        return send(sendable, null);
    }

    public Completable send(Sendable sendable, @Nullable Consumer<String> newId) {
        return this.send(Paths.chatMessagesPath(id), sendable, newId);
    }

    public Completable leave() {
        return removeUser(User.currentUser()).doOnComplete(this::disconnect);
    }

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a errorMessage is delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the errorMessage
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(type, messageId, null);
    }

    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(new DeliveryReceipt(type, messageId), newId);
    }

    public Completable markReceived(Message message) {
        return sendDeliveryReceipt(DeliveryReceiptType.received(), message.id);
    }

    @Override
    public Completable markRead(Message message) {
        return sendDeliveryReceipt(DeliveryReceiptType.read(), message.id);
    }

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Completable sendTypingIndicator(TypingStateType type) {
        return sendTypingIndicator(type, null);
    }

    public Completable sendTypingIndicator(TypingStateType type, @Nullable Consumer<String> newId) {
        return send(new TypingState(type), newId);
    }

    public Completable sendMessageWithText(String text) {
        return sendMessageWithText(text, null);
    }

    public Completable sendMessageWithText(String text, @Nullable Consumer<String> newId) {
        return send(new TextMessage(text), newId);
    }

    public Completable sendMessageWithBody(HashMap<String, Object> body) {
        return sendMessageWithBody(body, null);
    }

    public Completable sendMessageWithBody(HashMap<String, Object> body, @Nullable Consumer<String> newId) {
        return send(new Message(body), newId);
    }

    public RoleType getRoleTypeForUser(User theUser) {
        for (User user: users) {
            if (user.equals(theUser)) {
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

    public MultiQueueSubject<UserEvent> getUserEventStream() {
        return userEvents;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public Observable<String> getNameStream() {
        return nameStream.hide();
    }

    public Observable<String> getImageURLStream() {
        return imageURLStream.hide();
    }

    public ArrayList<FireStreamUser> getFireflyUsers() {
        ArrayList<FireStreamUser> fireStreamUsers = new ArrayList<>();
        for (User u : users) {
            fireStreamUsers.add(FireStreamUser.fromUser(u));
        }
        return fireStreamUsers;
    }

    public String getName() {
        return meta.name;
    }

    public String getImageURL() {
        return meta.imageURL;
    }


    public Path path() {
        return Paths.chatPath(id);
    }

    protected void setMeta(Meta meta) {
        this.meta = meta;
    }

}
