package sdk.chat.micro;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.micro.chat.AbstractChat;
import sdk.chat.micro.filter.MessageStreamFilter;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.Paths;
import sdk.chat.micro.chat.GroupChat;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TextMessage;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.PresenceType;
import sdk.chat.micro.types.SendableType;
import sdk.chat.micro.types.TypingStateType;

public class MicroChatSDK extends AbstractChat {

    static final MicroChatSDK instance = new MicroChatSDK();

    protected FirebaseUser user;

    public ArrayList<String> contacts = new ArrayList<>();
    public ArrayList<String> blocked = new ArrayList<>();

    protected PublishSubject<GroupChat> groupChatAddedStream = PublishSubject.create();
    protected PublishSubject<GroupChat> groupChatRemovedStream = PublishSubject.create();

    public static MicroChatSDK shared () {
        return instance;
    }

    public Listener contactsChangedListener;
    public Listener blockListChangedListener;

    protected ArrayList<GroupChat> groupChats = new ArrayList<>();

    public MicroChatSDK () {

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            this.user = firebaseAuth.getCurrentUser();
            if (this.user != null) {
                try {
                    connect();
                } catch (Exception e) {
                    errorStream.onNext(e);
                }
            } else {
                disconnect();
            }
        });
    }

    public void initialize() {
    }

    public void initialize(Config config) {
        this.config = config;
    }

    public void connect () throws Exception {

        if (this.user == null) {
            throw new Exception("A user must be authenticated to connect");
        }

        super.connect();

        // MESSAGE DELETION

        // We always delete typing state and delivery receipt messages
        Observable<Sendable> stream = sendableStream;
        if (!config.deleteMessagesOnReceipt) {
            stream = stream.filter(MessageStreamFilter.bySendableType(SendableType.typingState(), SendableType.deliveryReceipt()));
        }
        // If deletion is enabled, we don't filter so we delete all the message types
        dl.add(stream.flatMapCompletable(this::deleteSendable).subscribe());

        // DELIVERY RECEIPTS

        dl.add(messageStream.subscribe(message -> {
            // If delivery receipts are enabled, send the delivery receipt
            if (config.deliveryReceiptsEnabled) {
                dl.add(sendDeliveryReceipt(message.from, DeliveryReceiptType.received(), message.id)
                        .doOnError(MicroChatSDK.this)
                        .subscribe());
            }
            // If message deletion is disabled, instead mark the message as received. This means
            // that when we add a listener, we only get new messages
            if (!config.deleteMessagesOnReceipt) {
                dl.add(sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), message.id)
                        .doOnError(MicroChatSDK.this)
                        .subscribe());
            }
        }));

        // INVITATIONS

        dl.add(invitationStream.flatMapCompletable(invitation -> {
            if (config.autoAcceptGroupChatInvite) {
                return joinGroupChat(invitation.getGroupUid());
            }
            return Completable.complete();
        }).doOnError(this).subscribe());

        // BLOCKED USERS

        dl.add(listOn(Paths.blockedRef()).subscribe(map -> {
            blocked.clear();
            blocked.addAll(map.keySet());
            if (blockListChangedListener != null) {
                blockListChangedListener.onEvent();
            }
        }, this));

        // CONTACTS

        dl.add(listOn(Paths.contactsRef()).subscribe(map -> {
            contacts.clear();
            contacts.addAll(map.keySet());
            if (contactsChangedListener != null) {
                contactsChangedListener.onEvent();
            }
        }, this));

        // CONNECT TO EXISTING GROUP CHATS

        listenerRegistrations.add(Paths.userGroupChatsRef().addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                for (DocumentChange c : snapshot.getDocumentChanges()) {
                    DocumentSnapshot s = c.getDocument();
                    if (s.exists()) {

                        String groupChatId = s.getId();
                        GroupChat groupChat = getGroupChat(groupChatId);

                        if (c.getType() == DocumentChange.Type.ADDED) {
                            if (groupChat == null) {
                                groupChat = new GroupChat(groupChatId);
                            }
                            try {
                                groupChat.connect();
                                MicroChatSDK.this.groupChatAddedStream.onNext(groupChat);

                            } catch (Exception ex) {
                                MicroChatSDK.this.errorStream.onNext(ex);
                            }
                        }
                        if (c.getType() == DocumentChange.Type.REMOVED) {
                            if (groupChat != null) {
                                final GroupChat finalGroupChat = groupChat;
                                dl.add(groupChat.removeUser(currentUserId()).subscribe(() -> {
                                    groupChatRemovedStream.onNext(finalGroupChat);
                                }, MicroChatSDK.this));
                            }
                        }
                    }
                }
            }
        }));
   }

    @Override
    protected CollectionReference messagesRef() {
        return Paths.messagesRef();
    }

    public Completable deleteSendable (Sendable sendable) {
        return deleteSendable(Paths.messageRef(sendable.id));
    }

    public Single<String> sendPresence(String userId, PresenceType type) {
        return send(userId, new Presence(type));
    }

    public Single<String> sendInvitation(String userId, InvitationType type, String groupId) {
        return send(userId, new Invitation(type, groupId));
    }

    public Single<String> send(String toId, Sendable sendable) {
        return this.send(Paths.messagesRef(toId), sendable);
    }

    public String currentUserId() {
        return user.getUid();
    }

    public Completable block(String userId) {
        return addUserId(Paths.blockedRef(), userId, userId);
    }

    public Completable unblock(String userId) {
        return removeUserId(Paths.blockedRef(), userId);
    }

    public Completable addContact(String userId) {
        return addUserId(Paths.contactsRef(), userId, userId);
    }

    public Completable removeContact(String userId) {
        return removeUserId(Paths.contactsRef(), userId);
    }

    public ArrayList<String> getContacts() {
        return contacts;
    }

    public ArrayList<String> getBlocked() {
        return blocked;
    }

    public Single<GroupChat> createGroupChat(String name, String avatarURL, List<GroupChat.User> users) {
        return GroupChat.create(name, avatarURL, users).flatMap(groupChat -> {
            return joinGroupChat(groupChat.getId()).toSingle(() -> groupChat);
        });
    }

    public GroupChat getGroupChat(String id) {
        for (GroupChat groupChat : groupChats) {
            if (groupChat.getId().equals(id)) {
                return groupChat;
            }
        }
        return null;
    }

    public Completable leaveGroupChat(String id) {
        return Completable.create(emitter -> {
            Paths.userGroupChatsRef().document(id).delete().addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        });
    }

    protected Completable joinGroupChat(String id) {
        return Completable.create(emitter -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put(Keys.Date, FieldValue.serverTimestamp());
            Paths.userGroupChatsRef().document(id).set(data).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        });
    }

    public List<GroupChat> getGroupChats() {
        return groupChats;
    }

    /**
     * Send a delivery receipt to a user. If delivery receipts are enabled,
     * a 'received' status will be returned as soon as a message is delivered
     * and then you can then manually send a 'read' status when the user
     * actually reads the message
     * @param userId - the recipient user id
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Single<String> sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId) {
        return send(userId, new DeliveryReceipt(type, messageId));
    }

    /**
     * Send a typing indicator update to a user. This should be sent when the user
     * starts or stops typing
     * @param userId - the recipient user id
     * @param type - the status getBodyType
     * @return - subscribe to get a completion, error update from the method
     */
    public Single<String> sendTypingIndicator(String userId, TypingStateType type) {
        return send(userId, new TypingState(type));
    }

    public Single<String> sendMessageWithText(String userId, String text) {
        return send(userId, new TextMessage(text));
    }

    public Single<String> sendMessageWithBody(String userId, HashMap<String, Object> body) {
        return send(userId, new Message(body));
    }

    @Override
    protected Single<Date> dateOfLastDeliveryReceipt() {
        if (config.deleteMessagesOnReceipt) {
            return Single.just(new Date(0));
        } else {
            return super.dateOfLastDeliveryReceipt();
        }
    }

    public PublishSubject<GroupChat> getGroupChatAddedStream() {
        return groupChatAddedStream;
    }

    public PublishSubject<GroupChat> getGroupChatRemovedStream() {
        return groupChatRemovedStream;
    }

}
