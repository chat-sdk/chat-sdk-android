package firestream.chat.chat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import firefly.sdk.chat.R;
import firestream.chat.events.EventType;
import firestream.chat.events.UserEvent;
import firestream.chat.filter.MessageStreamFilter;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.Paths;
import firestream.chat.interfaces.IChat;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
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

public class Chat extends AbstractChat implements IChat {

    protected String id;
    protected Date joined;
    protected Meta meta = new Meta();

    protected ArrayList<User> users = new ArrayList<>();
    protected MultiQueueSubject<UserEvent> userEvents = MultiQueueSubject.create();

    protected BehaviorSubject<String> nameChangedEvents = BehaviorSubject.create();
    protected BehaviorSubject<String> imageURLChangedEvents = BehaviorSubject.create();
    protected BehaviorSubject<HashMap<String, Object>> customDataChangedEvents = BehaviorSubject.create();

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void connect() throws Exception {

        debug("Connect to chat: " + id);

        // If delivery receipts are enabled, send the delivery receipt
        if (Fire.privateApi().getConfig().deliveryReceiptsEnabled) {
            dm.add(getSendableEvents()
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

            // If we start by removing the user. If it isType a remove event
            // we leave it at that. Otherwise we add that user back in
            users.remove(user);
            if (!userEvent.typeIs(EventType.Removed)) {
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

                        if (newMeta.getName() != null && !newMeta.getName().equals(meta.getName())) {
                            meta.setName(newMeta.name);
                            nameChangedEvents.onNext(meta.getName());
                        }
                        if (newMeta.getImageURL() != null && !newMeta.getImageURL().equals(meta.getImageURL())) {
                            meta.setImageURL(newMeta.imageURL);
                            imageURLChangedEvents.onNext(meta.getImageURL());
                        }
                        if (newMeta.getData() != null) {
                            meta.setData(newMeta.getData());
                            customDataChangedEvents.onNext(meta.getData());
                        }
                        if (newMeta.getCreated() != null) {
                            meta.setCreated(newMeta.getCreated());
                        }
                    }
                }, this));

        super.connect();
    }

    @Override
    public Completable leave() {
        return removeUser(User.currentUser()).doOnComplete(this::disconnect);
    }

    @Override
    public String getName() {
        return meta.getName();
    }

    @Override
    public Completable setName(String name) {
        if (!testPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else if(this.meta.getName().equals(name)) {
            return Completable.complete();
        } else {
            return Fire.Stream.getFirebaseService().chat.setMetaField(metaPath(), Keys.Name, name).doOnComplete(() -> {
                meta.setName(name);
            });
        }
    }

    @Override
    public String getImageURL() {
        return meta.getImageURL();
    }

    @Override
    public Completable setImageURL(final String url) {
        if (!testPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else if (this.meta.getImageURL().equals(url)) {
            return Completable.complete();
        } else {
            return Fire.Stream.getFirebaseService().chat.setMetaField(metaPath(), Keys.ImageURL, url).doOnComplete(() -> {
                meta.setImageURL(url);
            });
        }
    }

    @Override
    public HashMap<String, Object> getCustomData() {
        return meta.getData();
    }

    @Override
    public Completable setCustomData(final HashMap<String, Object> data) {
        if (!testPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else {
            return Fire.Stream.getFirebaseService().chat.setMetaField(metaPath(), Paths.Data, data).doOnComplete(() -> {
                meta.setData(data);
            });
        }
    }

    @Override
    public ArrayList<User> getUsers() {
        return users;
    }

    @Override
    public ArrayList<FireStreamUser> getFireStreamUsers() {
        ArrayList<FireStreamUser> fireStreamUsers = new ArrayList<>();
        for (User u : users) {
            fireStreamUsers.add(FireStreamUser.fromUser(u));
        }
        return fireStreamUsers;
    }

    @Override
    public Completable addUser(Boolean sendInvite, User user) {
        return addUsers(sendInvite, user);
    }

    @Override
    public Completable addUsers(Boolean sendInvite, User... users) {
        return addUsers(sendInvite, Arrays.asList(users));
    }

    @Override
    public Completable addUsers(Boolean sendInvite, List<User> users) {
        return addUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users).concatWith(sendInvite ? inviteUsers(users) : Completable.complete()).doOnComplete(() -> {
            this.users.addAll(users);
        });
    }

    @Override
    public Completable updateUser(User user) {
        return updateUser(Paths.chatUsersPath(id), User.roleTypeDataProvider(), user);
    }

    @Override
    public Completable updateUsers(List<User> users) {
        return updateUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    @Override
    public Completable updateUsers(User... users) {
        return updateUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users);
    }

    @Override
    public Completable removeUser(User user) {
        return removeUser(Paths.chatUsersPath(id), user);
    }

    @Override
    public Completable removeUsers(User... user) {
        return removeUsers(Paths.chatUsersPath(id), user);
    }

    @Override
    public Completable removeUsers(List<User> user) {
        return removeUsers(Paths.chatUsersPath(id), user);
    }

    @Override
    public Completable inviteUsers(List<User> users) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (User user : users) {
            if (!user.isMe()) {
                completables.add(Fire.Stream.sendInvitation(user.id, InvitationType.chat(), id));
            }
        }
        return Completable.merge(completables).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
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
    public Completable setRole(User user, RoleType roleType) {
        if (roleType.equals(RoleType.owner()) && !testPermission(RoleType.owner())) {
            return Completable.error(this::ownerPermissionRequired);
        } else if(!testPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        }
        user.roleType = roleType;
        return updateUser(user);
    }

    @Override
    public RoleType getRoleTypeForUser(User theUser) {
        for (User user: users) {
            if (user.equals(theUser)) {
                return user.roleType;
            }
        }
        return null;
    }

    @Override
    public Observable<String> getNameChangeEvents() {
        return nameChangedEvents.hide();
    }

    @Override
    public Observable<String> getImageURLChangeEvents() {
        return imageURLChangedEvents.hide();
    }

    @Override
    public Observable<HashMap<String, Object>> getCustomDataChangedEvents() {
        return customDataChangedEvents.hide();
    }

    @Override
    public MultiQueueSubject<UserEvent> getUserEvents() {
        return userEvents;
    }

    @Override
    public Completable sendMessageWithBody(HashMap<String, Object> body) {
        return sendMessageWithBody(body, null);
    }

    @Override
    public Completable sendMessageWithBody(HashMap<String, Object> body, @Nullable Consumer<String> newId) {
        return send(new Message(body), newId);
    }

    @Override
    public Completable sendMessageWithText(String text) {
        return sendMessageWithText(text, null);
    }

    @Override
    public Completable sendMessageWithText(String text, @Nullable Consumer<String> newId) {
        return send(new TextMessage(text), newId);
    }

    @Override
    public Completable sendTypingIndicator(TypingStateType type) {
        return sendTypingIndicator(type, null);
    }

    @Override
    public Completable sendTypingIndicator(TypingStateType type, @Nullable Consumer<String> newId) {
        return send(new TypingState(type), newId);
    }

    @Override
    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(type, messageId, null);
    }

    @Override
    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(new DeliveryReceipt(type, messageId), newId);
    }

    @Override
    public Completable send(Sendable sendable, @Nullable Consumer<String> newId) {
        if (!testPermission(RoleType.member())) {
            return Completable.error(this::memberPermissionRequired);
        }
        return this.send(Paths.chatMessagesPath(id), sendable, newId);
    }

    @Override
    public Completable send(Sendable sendable) {
        return send(sendable, null);
    }

    @Override
    public Completable markReceived(Message message) {
        return sendDeliveryReceipt(DeliveryReceiptType.received(), message.getId());
    }

    @Override
    public Completable markRead(Message message) {
        return sendDeliveryReceipt(DeliveryReceiptType.read(), message.getId());
    }

    protected RoleType getMyRoleType() {
        return getRoleTypeForUser(Fire.Stream.currentUser());
    }

    @Override
    public boolean equals(Object chat) {
        if (chat instanceof Chat) {
            return id.equals(((Chat) chat).id);
        }
        return false;
    }

    protected void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Path path() {
        return Paths.chatPath(id);
    }

    public Path metaPath() {
        return Paths.chatMetaPath(id);
    }

    @Override
    protected Path messagesPath() {
        return Paths.chatMessagesPath(id);
    }

    protected Exception ownerPermissionRequired() {
        return new Exception(Fire.privateApi().context().getString(R.string.error_owner_permission_required));
    }

    protected Exception adminPermissionRequired() {
        return new Exception(Fire.privateApi().context().getString(R.string.error_admin_permission_required));
    }

    protected Exception memberPermissionRequired() {
        return new Exception(Fire.privateApi().context().getString(R.string.error_member_permission_required));
    }

    public static Single<Chat> create(final String name, final String imageURL, final HashMap<String, Object> data, final List<User> users) {

        return Fire.Stream.getFirebaseService().chat.add(Paths.chatsPath(), Meta.from(name, imageURL, data).addTimestamp().wrap().toData()).flatMap(chatId -> {
            Chat chat = new Chat(chatId, null, new Meta(name, imageURL, data));

            ArrayList<User> usersToAdd = new ArrayList<>(users);

            // Make sure the current user isType the owner
            usersToAdd.remove(User.currentUser());
            usersToAdd.add(User.currentUser(RoleType.owner()));

            return chat.addUsers(true, usersToAdd)
                    .toSingle(() -> chat);

        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    protected boolean testPermission(RoleType required) {
        RoleType myRoleType = getMyRoleType();
        if (myRoleType == null) {
            return false;
        }
        return getMyRoleType().ge(required);
    }

    public Completable deleteSendable(Sendable sendable) {
        return deleteSendable(messagesPath().child(sendable.getId()));
    }
}
