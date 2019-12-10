package sdk.chat.micro.chat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.micro.Config;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.Paths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.rx.DisposableList;
import sdk.chat.micro.types.DeliveryReceiptType;
import sdk.chat.micro.types.RoleType;
import sdk.chat.micro.types.SendableType;

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
    protected DisposableList dl = new DisposableList();

    protected PublishSubject<Message> messageStream = PublishSubject.create();
    protected PublishSubject<DeliveryReceipt> deliveryReceiptStream = PublishSubject.create();
    protected PublishSubject<TypingState> typingStateStream = PublishSubject.create();
    protected PublishSubject<Presence> presenceStream = PublishSubject.create();
    protected PublishSubject<Invitation> invitationStream = PublishSubject.create();

    protected PublishSubject<Sendable> sendableStream = PublishSubject.create();

    protected PublishSubject<Throwable> errorStream = PublishSubject.create();

    protected Config config = new Config();

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

    protected Observable<MessageResult> messagesOn() {
        return messagesOn(null);
    }

    protected Observable<MessageResult> messagesOn(Date newerThan) {
        return Observable.create(emitter -> {

            Query query = messagesRef().orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (newerThan != null) {
                query = query.whereGreaterThan(Keys.Date, newerThan);
            }
            query.limit(config.messageHistoryLimit);

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

            Query query = messagesRef.orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (fromDate != null) {
                query = query.whereGreaterThan(Keys.Date, fromDate);
            }
            if (toDate != null) {
                query = query.whereLessThan(Keys.Date, toDate);
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

    protected Single<Date> dateOfLastDeliveryReceipt() {
        return Single.create(emitter -> {
            Query query = messagesRef().whereEqualTo(Keys.Type, SendableType.DeliveryReceipt);

            query = query.whereEqualTo(Keys.From, MicroChatSDK.shared().currentUserId());
            query = query.orderBy(Keys.Date, Query.Direction.DESCENDING);
            query = query.limit(1);

            query.get().addOnSuccessListener(snapshot -> {
                if (snapshot.getDocumentChanges().size() > 0) {
                    DocumentChange change = snapshot.getDocumentChanges().get(0);
                    if (change.getDocument().exists()) {
                        Sendable sendable = change.getDocument().toObject(Sendable.class);
                        emitter.onSuccess(sendable.getDate());
                    }
                }
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(new Date(0));
                }
            }).addOnFailureListener(emitter::onError);
        });
    }

    protected Single<HashMap<String, HashMap<String, String>>> listOn(CollectionReference reference) {
        return Single.create(emitter -> listenerRegistrations.add(reference.addSnapshotListener((snapshot, e) -> {
            HashMap<String, HashMap<String, String>> values = new HashMap<>();
            if (snapshot != null) {
                for (DocumentSnapshot d : snapshot.getDocuments()) {
                    if (d.exists()) {
                        Map<String, Object> data = d.getData();
                        for (String key: data.keySet()) {
                            if (data.get(key) instanceof String) {
                                HashMap<String, String> value = new HashMap<>();
                                value.put(key, (String) data.get(key));
                                values.put(d.getId(), value);
                            }
                        }
                    }
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

    protected Completable deleteSendable (DocumentReference messagesRef) {
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

    protected Completable addUserIds(CollectionReference ref, List<ListUser> users) {
        WriteBatch batch = Paths.db().batch();

        for (ListUser u : users) {
            DocumentReference docRef = ref.document(u.id);
            batch.set(docRef, new RoleType(u.value).data());
        }

        return runBatch(batch);
    }

    protected Completable updateUserIds(CollectionReference ref, List<ListUser> users) {

        WriteBatch batch = Paths.db().batch();

        for (ListUser u : users) {
            DocumentReference docRef = ref.document(u.id);
            batch.update(docRef, new RoleType(u.value).data());
        }

        return runBatch(batch);
    }
    protected Completable removeUserIds(CollectionReference ref, List<String> userIds) {

        WriteBatch batch = Paths.db().batch();

        for (String id : userIds) {
            DocumentReference docRef = ref.document(id);
            batch.delete(docRef);
        }

        return runBatch(batch);
    }

    protected Completable runBatch(WriteBatch batch) {
        return Completable.create(emitter -> {
            batch.commit().addOnCompleteListener(task -> {
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

    public void connect() throws Exception {
        disconnect();

        //
        dl.add(dateOfLastDeliveryReceipt()
                .flatMapObservable(this::messagesOn)
                .doOnError(this)
                .subscribe(this::passMessageResultToStream));
    }

    public void disconnect () {
        for (ListenerRegistration lr : listenerRegistrations) {
            lr.remove();
        }
        listenerRegistrations.clear();
        dl.dispose();
    }

    protected void passMessageResultToStream(MessageResult mr) {

        Sendable sendable = mr.sendable;
        DocumentSnapshot s = mr.snapshot;

        if (sendable.type.equals(SendableType.Message)) {
            messageStream.onNext(messageForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.DeliveryReceipt)) {
            deliveryReceiptStream.onNext(deliveryReceiptForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.TypingState)) {
            typingStateStream.onNext(typingStateForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.Invitation)) {
            invitationStream.onNext(invitationForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.Presence)) {
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

    protected abstract CollectionReference messagesRef ();

}
