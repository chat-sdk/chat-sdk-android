package sdk.chat.micro.chat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.micro.Config;
import sdk.chat.micro.MicroChatSDK;
import sdk.chat.micro.User;
import sdk.chat.micro.events.Event;
import sdk.chat.micro.events.EventType;
import sdk.chat.micro.events.ListEvent;
import sdk.chat.micro.firestore.Keys;
import sdk.chat.micro.firestore.Paths;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.rx.DisposableList;
import sdk.chat.micro.types.SendableType;

public abstract class AbstractChat implements Consumer<Throwable> {

    public static class MessageResult {

        public Sendable sendable;
        public DocumentSnapshot snapshot;

        public MessageResult(DocumentSnapshot snapshot, Sendable sendable) {
            this.sendable = sendable;
            this.snapshot = snapshot;
        }
    }

    /**
     * Firestore listener registrations - so we can remove listeners on logout
     */
    protected ArrayList<ListenerRegistration> listenerRegistrations = new ArrayList<>();

    /**
     * Store the disposables so we can dispose of all of them when the user logs out
     */
    protected DisposableList dl = new DisposableList();

    /**
     * Event stream
     */
    protected Stream stream = new Stream();

    /**
     * A list of all sendables received
     */
    protected ArrayList<Sendable> sendables = new ArrayList<>();

    /**
     * Current configuration
     */
    protected Config config = new Config();

    /**
     * Error handler method so we can redirect all errors to the error stream
     * @param throwable - the stream error
     * @throws Exception
     */
    @Override
    public void accept(Throwable throwable) throws Exception {
        stream.errors.onError(throwable);
    }

    /**
     * Start listening to the current message reference and retrieve all messages
     * @return a stream of message results
     */
    protected Observable<MessageResult> messagesOn() {
        return messagesOn(null);
    }

    /**
     * Start listening to the current message reference and pass the messages to the stream
     * @param newerThan only listen for messages after this date
     * @return a stream of message results
     */
    protected Observable<MessageResult> messagesOn(Date newerThan) {
        return Observable.create((ObservableOnSubscribe<MessageResult>)emitter -> {

            Query query = messagesRef().orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (newerThan != null) {
                query = query.whereGreaterThan(Keys.Date, newerThan);
            }
            query.limit(config.messageHistoryLimit);

            listenerRegistrations.add(query.addSnapshotListener((snapshot, e) -> {
                if (snapshot != null) {
                    for (DocumentChange c : snapshot.getDocumentChanges()) {
                        DocumentSnapshot ds = c.getDocument();
                        // Add the message
                        if (c.getType() == DocumentChange.Type.ADDED) {
                            MessageResult mr = messageResultFromSnapshot(ds);
                            if (mr != null) {
                                getStream().getSendables().onNext(mr.sendable);
                            }
                            sendables.add(mr.sendable);
                            emitter.onNext(mr);
                        }
                    }
//                    for (DocumentSnapshot ds : snapshot.getDocuments()) {
//                        System.out.println("AAA");
//                        MessageResult mr = messageResultFromSnapshot(ds);
//                        sendables.add(mr.sendable);
//                    }
                } else if (e != null) {
                    stream.impl_throwablePublishSubject().onNext(e);
                }
            }));
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Convert a snapshot to a message result
     * @param snapshot Firestore snapshot
     * @return message result
     */
    protected MessageResult messageResultFromSnapshot(DocumentSnapshot snapshot) {
        if (snapshot.exists()) {
            Sendable sendable = snapshot.toObject(Sendable.class);
            if (sendable != null) {
                sendable.id = snapshot.getId();
                return new MessageResult(snapshot, sendable);
            }
        }
        return null;
    }

    /**
     * Get a batch of messages once
     * @param fromDate get messages from this date
     * @param toDate get messages until this date
     * @param limit limit the maximum number of messages
     * @return a stream of message results
     */
    public Observable<MessageResult> messagesOnce(@Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Observable.create((ObservableOnSubscribe<MessageResult>)emitter -> {

            Query query = messagesRef().orderBy(Keys.Date, Query.Direction.ASCENDING);
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * This method gets the date of the last delivery receipt that we sent - i.e. the
     * last message WE received.
     * @return single date
     */
    protected Single<Date> dateOfLastDeliveryReceipt() {
        return Single.create((SingleOnSubscribe<Date>) emitter -> {
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Listen for changes in the value of a list reference
     * @param ref the Firestore ref to listen to
     * @return stream of list events
     */
    protected Observable<ListEvent> listChangeOn(CollectionReference ref) {
        return Observable.create((ObservableOnSubscribe<ListEvent>) emitter -> {
            listenerRegistrations.add(ref.addSnapshotListener((snapshot, e) -> {
                if (snapshot != null) {
                    for (DocumentChange dc: snapshot.getDocumentChanges()) {
                        DocumentSnapshot d =  dc.getDocument();
                        if (d.exists()) {
                            EventType type = Event.typeForDocumentChange(dc);
                            emitter.onNext(new ListEvent(d.getId(), d.getData(), type));
                        }
                    }
                }
            }));
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Send a message to a messages ref
     * @param messagesRef Firestore reference for message collection
     * @param sendable item to be sent
     * @return single containing message id
     */
    public Single<String> send(CollectionReference messagesRef, Sendable sendable) {
        return Single.create((SingleOnSubscribe<String>)emitter -> messagesRef.add(sendable).addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                String messageId = task.getResult().getId();
                emitter.onSuccess(messageId);
            } else {
                emitter.onError(new Throwable("Message ID null"));
            }
        }).addOnFailureListener(emitter::onError))
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Delete a sendable from our queue
     * @param messagesRef Firestore reference for message collection
     * @return completion
     */
    protected Completable deleteSendable (DocumentReference messagesRef) {
        return Completable.create(emitter -> {
            messagesRef.delete().addOnSuccessListener(aVoid -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Remove a user from a reference
     * @param ref Firestore reference for users
     * @param user to remove
     * @return completion
     */
    protected Completable removeUser(CollectionReference ref, User user) {
        return removeUsers(ref, user);
    }

    /**
     * Remove users from a reference
     * @param ref Firestore reference for users
     * @param users to remove
     * @return completion
     */
    protected Completable removeUsers(CollectionReference ref, User... users) {
        return removeUsers(ref, Arrays.asList(users));
    }

    /**
     * Remove users from a reference
     * @param ref Firestore reference for users
     * @param users to remove
     * @return completion
     */
    protected Completable removeUsers(CollectionReference ref, List<User> users) {

        WriteBatch batch = Paths.db().batch();

        for (User u : users) {
            DocumentReference docRef = ref.document(u.id);
            batch.delete(docRef);
        }

        return runBatch(batch);
    }

    /**
     * Add a user to a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param user to add
     * @return completion
     */
    protected Completable addUser(CollectionReference ref, User.DataProvider dataProvider, User user) {
        return addUsers(ref, dataProvider, user);
    }

    /**
     * Add users to a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to add
     * @return completion
     */
    public Completable addUsers(CollectionReference ref, User.DataProvider dataProvider, User... users) {
        return addUsers(ref, dataProvider, Arrays.asList(users));
    }

    /**
     * Add users to a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to add
     * @return completion
     */
    public Completable addUsers(CollectionReference ref, User.DataProvider dataProvider, List<User> users) {
        WriteBatch batch = Paths.db().batch();

        for (User u : users) {
            DocumentReference docRef = ref.document(u.id);
            batch.set(docRef, dataProvider.data(u));
        }

        return runBatch(batch);
    }

    /**
     * Updates a user for a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param user to update
     * @return completion
     */
    public Completable updateUser(CollectionReference ref, User.DataProvider dataProvider, User user) {
        return updateUsers(ref, dataProvider, user);
    }

    /**
     * Update users for a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to update
     * @return completion
     */
    public Completable updateUsers(CollectionReference ref, User.DataProvider dataProvider, User... users) {
        return updateUsers(ref, dataProvider, Arrays.asList(users));
    }

    /**
     * Update users for a reference
     * @param ref Firestore reference for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to update
     * @return completion
     */
    public Completable updateUsers(CollectionReference ref, User.DataProvider dataProvider, List<User> users) {
        WriteBatch batch = Paths.db().batch();

        for (User u : users) {
            DocumentReference docRef = ref.document(u.id);
            batch.update(docRef, dataProvider.data(u));
        }

        return runBatch(batch);
    }

    /**
     * Run a Firestore batch operation
     * @param batch Firestore batch
     * @return completion
     */
    protected Completable runBatch(WriteBatch batch) {
        return Completable.create(emitter -> {
            batch.commit().addOnCompleteListener(task -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Send a sendable
     * @param toUserId user to send the message to
     * @param sendable item to send
     * @return single with message Id
     */
    public abstract Single<String> send(String toUserId, Sendable sendable);

    /**
     * Connect to the chat
     * @throws Exception error if we are not connected
     */
    public void connect() throws Exception {
        dl.add(dateOfLastDeliveryReceipt()
                .flatMapObservable(this::messagesOn)
                .subscribe(this::passMessageResultToStream, this));
    }

    /**
     * Disconnect from a chat
     */
    public void disconnect () {
        for (ListenerRegistration lr : listenerRegistrations) {
            lr.remove();
        }
        listenerRegistrations.clear();
        dl.dispose();
    }

    /**
     * Convenience method to cast sendables and send them to the correct stream
     * @param mr message result
     */
    protected void passMessageResultToStream(MessageResult mr) {

        Sendable sendable = mr.sendable;
        DocumentSnapshot s = mr.snapshot;

        if (sendable.type.equals(SendableType.Message)) {
            stream.getMessages().onNext(messageForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.DeliveryReceipt)) {
            stream.getDeliveryReceipts().onNext(deliveryReceiptForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.TypingState)) {
            stream.getTypingStates().onNext(typingStateForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.Invitation)) {
            stream.getInvitations().onNext(invitationForSnapshot(s));
        }
        if (sendable.type.equals(SendableType.Presence)) {
            stream.getPresences().onNext(presenceForSnapshot(s));
        }

    }

    /**
     * Convert a snapshot to a message
     * @param s Firestore snapshot
     * @return message
     */
    protected Message messageForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Message.class).parse(s);
    }

    /**
     * Convert a snapshot to a delivery receipt
     * @param s Firestore snapshot
     * @return delivery receipt
     */
    protected DeliveryReceipt deliveryReceiptForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(DeliveryReceipt.class).parse(s);
    }

    /**
     * Convert a snapshot to a typing state
     * @param s Firestore snapshot
     * @return typing state
     */
    protected TypingState typingStateForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(TypingState.class).parse(s);
    }

    /**
     * Convert a snapshot to an invitation
     * @param s Firestore snapshot
     * @return invitation
     */
    protected Invitation invitationForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Invitation.class).parse(s);
    }

    /**
     * Convert a snapshot to an presence
     * @param s Firestore snapshot
     * @return presence
     */
    protected Presence presenceForSnapshot(DocumentSnapshot s) {
        return new SnapshotParser<>(Presence.class).parse(s);
    }

    /**
     * Helper class to parse snapshots
     * @param <T> sendable type
     */
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

    /**
     * returns the stream object which exposes the different sendable streams
     * @return stream
     */
    public Stream getStream() {
        return stream;
    }

    /**
     * Overridable messages reference
     * @return Firestore messages reference
     */
    protected abstract CollectionReference messagesRef ();

}
