package firestream.chat.chat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import firestream.chat.R;
import firestream.chat.events.ListData;
import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.Paths;
import firestream.chat.interfaces.IChat;
import firestream.chat.message.Body;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.message.TextMessage;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.Fire;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.InvitationType;
import firestream.chat.types.RoleType;
import firestream.chat.types.TypingStateType;
import firestream.chat.util.Typing;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import sdk.guru.common.Event;

public class Chat extends AbstractChat implements IChat {

    protected String id;
    protected Date joined;
    protected Meta meta = new Meta();

    protected List<User> users = new ArrayList<>();
    protected MultiQueueSubject<Event<User>> userEvents = MultiQueueSubject.create();

    protected BehaviorSubject<String> nameChangedEvents = BehaviorSubject.create();
    protected BehaviorSubject<String> imageURLChangedEvents = BehaviorSubject.create();
    protected BehaviorSubject<Map<String, Object>> customDataChangedEvents = BehaviorSubject.create();

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
        if (Fire.stream().getConfig().deliveryReceiptsEnabled) {
            getSendableEvents()
                    .getMessages()
                    .pastAndNewEvents()
                    .filter(deliveryReceiptFilter())
                    .flatMapCompletable(messageEvent -> markReceived(messageEvent.get()))
                    .subscribe(this);
        }

        dm.add(listChangeOn(Paths.chatUsersPath(id)).subscribe(listEvent -> {
            Event<User> userEvent = listEvent.to(User.from(listEvent));
            User user = userEvent.get();

            // If we start by removing the user. If it type a remove event
            // we leave it at that. Otherwise we add that user back in
            users.remove(user);
            if (!userEvent.isRemoved()) {
                users.add(user);
            }

            userEvents.onNext(userEvent);
        }));

        // Handle name and image change
        dm.add(Fire.stream().getFirebaseService().chat
                .metaOn(getId())
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
        return Completable.defer(() -> {
            if (getMyRoleType().equals(RoleType.owner()) && getUsers().size() > 1) {
                if (getUsers().size() > 1) {
                    return Completable.error(Fire.internal().getError(R.string.error_group_must_be_empty_to_close));
                } else {
                    return delete().doOnComplete(this::disconnect);
                }
            }
            return removeUser(User.currentUser()).doOnComplete(this::disconnect);
        });
    }

    protected Completable delete() {
        return Fire.stream().getFirebaseService().chat.delete(getId());
    }

    @Override
    public String getName() {
        return meta.getName();
    }

    @Override
    public Completable setName(String name) {
        if (!hasPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else if(this.meta.getName().equals(name)) {
            return Completable.complete();
        } else {
            return Fire.stream().getFirebaseService().chat.setMetaField(getId(), Keys.Name, name).doOnComplete(() -> {
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
        if (!hasPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else if (this.meta.getImageURL().equals(url)) {
            return Completable.complete();
        } else {
            return Fire.stream().getFirebaseService().chat.setMetaField(getId(), Keys.ImageURL, url).doOnComplete(() -> {
                meta.setImageURL(url);
            });
        }
    }

    @Override
    public Map<String, Object> getCustomData() {
        return meta.getData();
    }

    @Override
    public Completable setCustomData(final Map<String, Object> data) {
        if (!hasPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        } else {
            return Fire.stream().getFirebaseService().chat.setMetaField(getId(), Paths.Data, data).doOnComplete(() -> {
                meta.setData(data);
            });
        }
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public List<FireStreamUser> getFireStreamUsers() {
        List<FireStreamUser> fireStreamUsers = new ArrayList<>();
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
    public Completable addUsers(Boolean sendInvite, List<? extends User> users) {
        return addUsers(Paths.chatUsersPath(id), User.roleTypeDataProvider(), users).concatWith(sendInvite ? inviteUsers(users) : Completable.complete()).doOnComplete(() -> {
            this.users.addAll(users);
        });
    }

    @Override
    public Completable updateUser(User user) {
        return updateUser(Paths.chatUsersPath(id), User.roleTypeDataProvider(), user);
    }

    @Override
    public Completable updateUsers(List<? extends User> users) {
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
    public Completable removeUsers(List<? extends User> users) {
        return removeUsers(Paths.chatUsersPath(id), users);
    }

    @Override
    public Completable inviteUsers(List<? extends User> users) {
        return Completable.defer(() -> {
            List<Completable> completables = new ArrayList<>();
            for (User user : users) {
                if (!user.isMe()) {
                    completables.add(Fire.stream().sendInvitation(user.id, InvitationType.chat(), id));
                }
            }
            return Completable.merge(completables);
        });
    }

    @Override
    public List<User> getUsersForRoleType(RoleType roleType) {
        List<User> result = new ArrayList<>();
        for (User user: users) {
            if (user.roleType.equals(roleType)) {
                result.add(user);
            }
        }
        return result;
    }

    @Override
    public Completable setRole(User user, RoleType roleType) {
        if (roleType.equals(RoleType.owner()) && !hasPermission(RoleType.owner())) {
            return Completable.error(this::ownerPermissionRequired);
        } else if(!hasPermission(RoleType.admin())) {
            return Completable.error(this::adminPermissionRequired);
        }
        user.roleType = roleType;
        return updateUser(user);
    }

    @Override
    public RoleType getRoleType(User theUser) {
        for (User user: users) {
            if (user.equals(theUser)) {
                return user.roleType;
            }
        }
        return null;
    }

    @Override
    public List<RoleType> getAvailableRoles(User user) {
        // We can't set our own role and only admins and higher can set a role
        if (!user.isMe() && hasPermission(RoleType.admin())) {
            // The owner can set users to any role apart from owner
            if (hasPermission(RoleType.owner())) {
                return RoleType.allExcluding(RoleType.owner());
            }
            // Admins can set the role type of non-admin users. They can't create or
            // destroy admins, only the owner can do that
            if (!user.roleType.equals(RoleType.admin())) {
                return RoleType.allExcluding(RoleType.owner(), RoleType.admin());
            }
        }
        return new ArrayList<>();
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
    public Observable<Map<String, Object>> getCustomDataChangedEvents() {
        return customDataChangedEvents.hide();
    }

    @Override
    public MultiQueueSubject<Event<User>> getUserEvents() {
        return userEvents;
    }

    @Override
    public Completable sendMessageWithBody(Body body) {
        return sendMessageWithBody(body, null);
    }

    @Override
    public Completable sendMessageWithBody(Body body, @Nullable Consumer<String> newId) {
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
    public Completable startTyping() {
        return Completable.defer(() -> {
            final Typing typing = typingMap.get(id);
            if (!typing.isTyping) {
                typing.isTyping = true;
                return send(new TypingState(TypingStateType.typing()), s -> {
                    typing.sendableId = s;
                });
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable stopTyping() {
        return Completable.defer(() -> {
            final Typing typing = typingMap.get(id);
            if (typing.isTyping) {
                return deleteSendable(typing.sendableId).doOnComplete(() -> {
                    typing.isTyping = false;
                    typing.sendableId = null;
                });
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable sendDeliveryReceipt(String fromUserId, DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(fromUserId, type, messageId, null);
    }

    @Override
    public Completable sendDeliveryReceipt(String fromUserId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return Fire.stream().sendDeliveryReceipt(fromUserId, type, messageId, newId);
    }

    @Override
    public Completable send(Sendable sendable, @Nullable Consumer<String> newId) {
        if (!hasPermission(RoleType.member())) {
            return Completable.error(this::memberPermissionRequired);
        }
        return this.send(Paths.chatMessagesPath(id), sendable, newId);
    }

    @Override
    public Completable send(Sendable sendable) {
        return send(sendable, null);
    }

    @Override
    public Completable markReceived(Sendable sendable) {
        return markReceived(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markReceived(String fromUserId, String sendableId) {
        return sendDeliveryReceipt(fromUserId, DeliveryReceiptType.received(), sendableId);
    }

    @Override
    public Completable markRead(Sendable sendable) {
        return markRead(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markRead(String fromUserId, String sendableId) {
        return sendDeliveryReceipt(fromUserId, DeliveryReceiptType.read(), sendableId);
    }

    public RoleType getMyRoleType() {
        return getRoleType(Fire.stream().currentUser());
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
        return new Exception(Fire.internal().context().getString(R.string.error_owner_permission_required));
    }

    protected Exception adminPermissionRequired() {
        return new Exception(Fire.internal().context().getString(R.string.error_admin_permission_required));
    }

    protected Exception memberPermissionRequired() {
        return new Exception(Fire.internal().context().getString(R.string.error_member_permission_required));
    }

    public static Single<Chat> create(final String name, final String imageURL, final Map<String, Object> data, final List<? extends User> users) {

        return Fire.stream().getFirebaseService().chat.add(Meta.from(name, imageURL, data).addTimestamp().wrap().toData()).flatMap(chatId -> {
            Chat chat = new Chat(chatId, null, new Meta(name, imageURL, data));

            List<User> usersToAdd = new ArrayList<>(users);

            // Make sure the current user type the owner
            usersToAdd.remove(User.currentUser());
            usersToAdd.add(User.currentUser(RoleType.owner()));

            return chat.addUsers(true, usersToAdd)
                    .toSingle(() -> chat);

        });
    }

    public boolean hasPermission(RoleType required) {
        RoleType myRoleType = getMyRoleType();
        if (myRoleType == null) {
            return false;
        }
        return getMyRoleType().ge(required);
    }

    public Completable deleteSendable(Sendable sendable) {
        return deleteSendable(sendable.getId());
    }

    public Completable deleteSendable(String sendableId) {
        return deleteSendable(messagesPath().child(sendableId));
    }

    public static Chat from(Event<ListData> listEvent) {
        ListData change = listEvent.get();
        if (change.get(Keys.Date) instanceof Date) {
            return new Chat(change.getId(), (Date) change.get(Keys.Date));
        }
        return new Chat(change.getId());
    }

    public Completable mute() {
        return Fire.internal().mute(getId());
    }

    public Completable mute(@Nullable Date until) {
        return Fire.internal().mute(getId(), until);
    }

    public Completable unmute() {
        return Fire.internal().unmute(getId());
    }

    public Date mutedUntil() {
        return Fire.internal().mutedUntil(getId());
    }

    public boolean muted() {
        return Fire.internal().muted(getId());
    }

}
