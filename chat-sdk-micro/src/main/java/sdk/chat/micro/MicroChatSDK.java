package sdk.chat.micro;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firestore.v1.DocumentTransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.micro.chat.AbstractChat;
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
import sdk.chat.micro.rx.DisposableList;
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
        disconnect();

        if (this.user == null) {
            throw new Exception("A user must be authenticated to connect");
        }

        // Get the date of the last received message
        if (!config.deleteMessagesOnReceipt) {
            Query messagesRef = Paths.messagesRef().whereEqualTo(Keys.Type, SendableType.DeliveryReceipt);
            messagesRef = messagesRef.whereEqualTo(Keys.From, MicroChatSDK.shared().currentUserId());
            messagesRef = messagesRef.orderBy(Keys.Date, Query.Direction.DESCENDING);
            messagesRef = messagesRef.limit(1);

            messagesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot snapshots) {
                    System.out.println("Test");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("Test");
                }
            });
        }

        disposableList.add(messagesOn(Paths.messagesRef()).subscribe(mr -> {

            passMessageResultToStream(mr);

            Sendable sendable = mr.sendable;

            // The message has been received, so now delete it from the server
            if (config.deleteMessagesOnReceipt || sendable.type.equals(SendableType.TypingState) || sendable.type.equals(SendableType.Presence)) {
                disposableList.add(deleteSendable(sendable).doOnError(MicroChatSDK.this).subscribe());
            } else {
                // If message deletion is disabled, instead mark the message as received. This means
                // that when we add a listener, we only get new messages
                disposableList.add(sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), sendable.id).doOnError(MicroChatSDK.this).subscribe());
            }
        }, this));

        disposableList.add(messageStream.subscribe(message -> {
            // If delivery receipts are enabled, send the delivery receipt
            if (config.deliveryReceiptsEnabled) {
                disposableList.add(
                        sendDeliveryReceipt(message.from, DeliveryReceiptType.received(), message.id).doOnError(MicroChatSDK.this).subscribe()
                );
            }
        }));

        disposableList.add(invitationStream.subscribe(invitation -> {
            if (config.autoAcceptGroupChatInvite) {
                joinGroupChat(invitation.getGroupUid())
                        .doOnError(MicroChatSDK.this)
                        .subscribe();
            }
        }));


        disposableList.add(userListOn(Paths.blockedRef()).subscribe(map -> {
            blocked.clear();
            blocked.addAll(map.keySet());
            if (blockListChangedListener != null) {
                blockListChangedListener.onEvent();
            }
        }, this));

        disposableList.add(userListOn(Paths.contactsRef()).subscribe(map -> {
            contacts.clear();
            contacts.addAll(map.keySet());
            if (contactsChangedListener != null) {
                contactsChangedListener.onEvent();
            }
        }, this));

        // Connect to our group chats
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
                                disposableList.add(groupChat.removeUser(currentUserId()).subscribe(() -> {
                                    groupChatRemovedStream.onNext(finalGroupChat);
                                }, MicroChatSDK.this));
                            }
                        }
                    }
                }
            }
        }));
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

//    protected Completable markReceived(Sendable sendable) {
//        return Completable.create(emitter -> {
//            HashMap<String, Object> data = new HashMap<>();
//            data.put(DeliveryReceiptType.Received, FieldValue.serverTimestamp());
//
//            Paths.messageRef(sendable.id).set(data, SetOptions.merge()).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
//        });
//    }



}
