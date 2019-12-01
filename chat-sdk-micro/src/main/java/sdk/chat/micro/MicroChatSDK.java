package sdk.chat.micro;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.micro.firestore.FSPaths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.PresenceType;
import sdk.chat.micro.types.SendableType;
import sdk.chat.micro.message.TypingState;

public class MicroChatSDK extends AbstractChat {

    static final MicroChatSDK instance = new MicroChatSDK();
    protected Config config = new Config();

    protected FirebaseUser user;

    public ArrayList<String> contacts = new ArrayList<>();
    public ArrayList<String> blocked = new ArrayList<>();

    public static MicroChatSDK shared () {
        return instance;
    }

    public Listener contactsChangedListener;
    public Listener blockListChangedListener;

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

        disposableList.add(messagesOn(FSPaths.messagesRef()).subscribe(mr -> {

            passMessageResultToStream(mr);

            Sendable sendable = mr.sendable;
            DocumentSnapshot s = mr.snapshot;

            if (sendable.type == SendableType.Message) {
                // If delivery receipts are enabled, send the delivery receipt
                if (config.deliveryReceiptsEnabled) {
                    Message message = messageForSnapshot(s);
                    disposableList.add(
                            sendDeliveryReceipt(sendable.fromId, DeliveryReceiptType.received(), message.id).doOnError(MicroChatSDK.this).subscribe()
                    );
                }
            }


            // The message has been received, so now delete it from the server
            disposableList.add(deleteSendable(sendable).doOnError(MicroChatSDK.this).subscribe());
        }, this));

        disposableList.add(userListOn(FSPaths.blockedRef()).subscribe(map -> {
            blocked.clear();
            blocked.addAll(map.keySet());
            if (blockListChangedListener != null) {
                blockListChangedListener.onEvent();
            }
        }, this));

        disposableList.add(userListOn(FSPaths.contactsRef()).subscribe(map -> {
            contacts.clear();
            contacts.addAll(map.keySet());
            if (contactsChangedListener != null) {
                contactsChangedListener.onEvent();
            }
        }, this));

    }

    public Completable deleteSendable (Sendable sendable) {
        return deleteSendable(FSPaths.messageRef(sendable.id), sendable);
    }

    public Single<String> sendPresence(String userId, PresenceType type) {
        return send(userId, new Presence(type));
    }

    public Single<String> sendInvitation(String userId, InvitationType type, String groupId) {
        return send(userId, new Invitation(type, groupId));
    }

    public Single<String> send(String toId, Sendable sendable) {
        return this.send(FSPaths.messagesRef(toId), sendable);
    }

    public String currentUserId() {
        return user.getUid();
    }

    public Completable block(String userId) {
        return addUserId(FSPaths.blockedRef(), userId, userId);
    }

    public Completable unblock(String userId) {
        return removeUserId(FSPaths.blockedRef(), userId);
    }

    public Completable addContact(String userId) {
        return addUserId(FSPaths.contactsRef(), userId, userId);
    }

    public Completable removeContact(String userId) {
        return removeUserId(FSPaths.contactsRef(), userId);
    }

    public ArrayList<String> getContacts() {
        return contacts;
    }

    public ArrayList<String> getBlocked() {
        return blocked;
    }

}
