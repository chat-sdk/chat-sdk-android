package sdk.chat.micro;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.micro.firestore.FSKeys;
import sdk.chat.micro.firestore.FSPaths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TextMessage;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.rx.DisposableList;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.SendableType;
import sdk.chat.micro.types.TypingStateType;

public abstract class AbstractChat implements Consumer<Throwable> {

    public interface Listener {
        void onEvent();
    }

    public static class MessageResult {

        public Sendable sendable;
        public DocumentSnapshot snapshot;

        public MessageResult(DocumentSnapshot snapshot, Sendable sendable) {
            this.sendable = sendable;
            this.snapshot = snapshot;
        }
    }

    protected ArrayList<ListenerRegistration> listenerRegistrations = new ArrayList<>();
    protected DisposableList disposableList = new DisposableList();

    protected PublishSubject<Message> messageStream = PublishSubject.create();
    protected PublishSubject<DeliveryReceipt> deliveryReceiptStream = PublishSubject.create();
    protected PublishSubject<TypingState> typingStateStream = PublishSubject.create();
    protected PublishSubject<Presence> presenceStream = PublishSubject.create();
    protected PublishSubject<Invitation> invitationStream = PublishSubject.create();

    protected PublishSubject<Sendable> sendableStream = PublishSubject.create();

    protected PublishSubject<Throwable> errorStream = PublishSubject.create();

    @Override
    public void accept(Throwable throwable) throws Exception {
        errorStream.onNext(throwable);
    }

    public PublishSubject<Throwable> getErrorStream() {
        return errorStream;
    }

    public PublishSubject<Message> getMessageStream() {
        return messageStream;
    }

    public PublishSubject<DeliveryReceipt> getDeliveryReceiptStream() {
        return deliveryReceiptStream;
    }

    public PublishSubject<TypingState> getTypingStateStream() {
        return typingStateStream;
    }

    public PublishSubject<Sendable> getSendableStream() {
        return sendableStream;
    }

    protected Observable<MessageResult> messagesOn(CollectionReference messagesRef) {
        return messagesOn(messagesRef, null);
    }

    protected Observable<MessageResult> messagesOn(CollectionReference messagesRef, Date newerThan) {
        return Observable.create(emitter -> {

            Query query = messagesRef.orderBy(FSKeys.Date, Query.Direction.ASCENDING);
            if (newerThan != null) {
                query = query.whereGreaterThan(FSKeys.Date, newerThan);
            }

            listenerRegistrations.add(query.addSnapshotListener((snapshot, e) -> {
                if (snapshot != null) {
                    for (DocumentChange c : snapshot.getDocumentChanges()) {
                        DocumentSnapshot s = c.getDocument();
                        // Add the message
                        if (s.exists() && c.getType() == DocumentChange.Type.ADDED) {
                            Sendable sendable = s.toObject(Sendable.class);
                            sendable.id = s.getId();

                            sendableStream.onNext(sendable);

                            emitter.onNext(new MessageResult(s, sendable));
                        }
                    }
                } else if (e != null) {
                    errorStream.onNext(e);
                }
            }));
        });
    }

    protected Observable<MessageResult> messagesOnce(CollectionReference messagesRef, Date fromDate, Date toDate, Integer limit) {
        return Observable.create(emitter -> {

            Query query = messagesRef.orderBy(FSKeys.Date, Query.Direction.ASCENDING);
            if (fromDate != null) {
                query = query.whereGreaterThan(FSKeys.Date, fromDate);
            }
            if (toDate != null) {
                query = query.whereLessThan(FSKeys.Date, toDate);
            }
            if (limit != null) {
                query = query.limit(limit);
            }

            query.get().addOnSuccessListener(snapshot -> {
                if (snapshot != null) {
                    for (DocumentChange c : snapshot.getDocumentChanges()) {
                        DocumentSnapshot s = c.getDocument();
                        // Add the message
                        if (s.exists() && c.getType() == DocumentChange.Type.ADDED) {
                            Sendable sendable = s.toObject(Sendable.class);
                            sendable.id = s.getId();
                            emitter.onNext(new MessageResult(s, sendable));
                        }
                    }
                }
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        });
    }

    protected Single<HashMap<String, String>> userListOn(CollectionReference reference) {
        return Single.create(emitter -> listenerRegistrations.add(reference.addSnapshotListener((snapshot, e) -> {
            HashMap<String, String> values = new HashMap<>();
            if (snapshot != null) {
                for (DocumentSnapshot d : snapshot.getDocuments()) {
                    Map data = d.getData();
//                            values.put(d.getId(), d.getData())
                }
            }
            emitter.onSuccess(values);
        })));
    }

    public Single<String> send(CollectionReference messagesRef, Sendable sendable) {
        return Single.create(emitter -> messagesRef.add(sendable).addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                String messageId = task.getResult().getId();
                emitter.onSuccess(messageId);
            } else {
                emitter.onError(new Throwable("Message ID null"));
            }
        }).addOnFailureListener(emitter::onError));
    }

    protected Completable deleteSendable (DocumentReference messagesRef, Sendable sendable) {
        return Completable.create(emitter -> {
            messagesRef.delete().addOnSuccessListener(aVoid -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        });
    }

    protected Completable addUserId(CollectionReference ref, String userId, String value) {
        return Completable.create(emitter -> {
            ref.document(userId).set(value).addOnCompleteListener(task -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        });
    }

    protected Completable updateUserId(CollectionReference ref, String userId, String value) {
        return Completable.create(emitter -> {
            Map<String, Object> data = new HashMap<>();
            data.put(userId, value);
            ref.document(userId).update(data).addOnCompleteListener(task -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        });
    }
    protected Completable removeUserId(CollectionReference ref, String userId) {
        return Completable.create(emitter -> {
            ref.document(userId).delete().addOnCompleteListener(task -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        });
    }

    public abstract Single<String> send(String userId, Sendable sendable);

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

    public abstract void connect() throws Exception;

    public void disconnect () {
        for (ListenerRegistration lr : listenerRegistrations) {
            lr.remove();
        }
        listenerRegistrations.clear();
        disposableList.dispose();
    }

    protected void passMessageResultToStream(MessageResult mr) {

        Sendable sendable = mr.sendable;
        DocumentSnapshot s = mr.snapshot;

        if (sendable.type == SendableType.Message) {
            messageStream.onNext(messageForSnapshot(s));
        }
        if (sendable.type == SendableType.DeliveryReceipt) {
            deliveryReceiptStream.onNext(deliveryReceiptForSnapshot(s));
        }
        if (sendable.type == SendableType.TypingState) {
            typingStateStream.onNext(typingStateForSnapshot(s));
        }
        if (sendable.type == SendableType.Invitation) {
            invitationStream.onNext(invitationForSnapshot(s));
        }
        if (sendable.type == SendableType.Presence) {
            presenceStream.onNext(presenceForSnapshot(s));
        }
    }

    protected Message messageForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Message.class).parse(s);
    }

    protected DeliveryReceipt deliveryReceiptForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(DeliveryReceipt.class).parse(s);
    }

    protected TypingState typingStateForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(TypingState.class).parse(s);
    }

    protected Invitation invitationForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Invitation.class).parse(s);
    }

    protected Presence presenceForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Presence.class).parse(s);
    }

    public class SnapshotParser<T extends Sendable> {

        protected final Class<T> type;

        public SnapshotParser(Class<T> type) {
            this.type = type;
        }

        public T parse(DocumentSnapshot s) {
            T sendable = s.toObject(type);
            if (sendable != null) {
                sendable.id = s.getId();
            }
            return sendable;
        }
    }

}
