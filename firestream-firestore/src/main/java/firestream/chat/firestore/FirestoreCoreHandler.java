package firestream.chat.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import firestream.chat.chat.User;
import firestream.chat.events.ListData;
import firestream.chat.firebase.service.FirebaseCoreHandler;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.message.Body;
import firestream.chat.message.Sendable;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;
import sdk.guru.firestore.RXFirestore;

public class FirestoreCoreHandler extends FirebaseCoreHandler {

    @Override
    public Observable<Event<ListData>> listChangeOn(Path path) {
        return new RXFirestore().on(Ref.collection(path)).flatMapMaybe(change -> {
            DocumentSnapshot d = change.getDocument();
            if (d.exists()) {
                EventType type = FirestoreCoreHandler.typeForDocumentChange(change);
                if (type != null) {
                    return Maybe.just(new Event<>(new ListData(d.getId(), d.getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)), type));
                }
            }
            return Maybe.empty();
        });
    }

    @Override
    public Completable deleteSendable (Path messagesPath) {
        return new RXFirestore().delete(Ref.document(messagesPath));
    }

    @Override
    public Completable send(Path messagesPath, Sendable sendable, Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(messagesPath), sendable.toData(), newId).ignoreElement();
    }

    @Override
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.getId());
                batch.set(docRef, dataProvider.data(u));
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.getId());
                batch.update(docRef, dataProvider.data(u));
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Completable removeUsers(Path path, List<? extends User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.getId());
                batch.delete(docRef);
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Single<List<Sendable>> loadMoreMessages(Path messagesPath, @Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (fromDate != null) {
                query = query.whereGreaterThan(Keys.Date, fromDate);
            }
            if (toDate != null) {
                query = query.whereLessThanOrEqualTo(Keys.Date, toDate);
            }

            if (limit != null) {
                if (fromDate != null) {
                    query = query.limit(limit);
                }
                if (toDate != null) {
                    query = query.limitToLast(limit);
                }
            }

            emitter.onSuccess(query);
        }).flatMap(query -> new RXFirestore().get(query)).map(optional -> {
            List<Sendable> sendables = new ArrayList<>();
            if (!optional.isEmpty()) {
                QuerySnapshot snapshots = optional.get();
                if (!snapshots.isEmpty()) {
                    for (DocumentChange c : snapshots.getDocumentChanges()) {
                        DocumentSnapshot snapshot = c.getDocument();
                        // Add the message
                        if (snapshot.exists() && c.getType() == DocumentChange.Type.ADDED) {
                            Sendable sendable = snapshot.toObject(Sendable.class);
                            sendable.setId(snapshot.getId());
                            sendables.add(sendable);
                        }
                    }
                }
            }
            return sendables;
        });
    }

    @Override
    public Single<Optional<Sendable>> lastMessage(Path messagesPath) {
        return Single.defer(() -> {
            Query query = Ref.collection(messagesPath);

            query = query.orderBy(Keys.Date, Query.Direction.DESCENDING);
            query = query.limit(1);

            return new RXFirestore().get(query).map(snapshots -> {
                // TODO: Test this because with realtime the snapshot wasn't being handled properly
                if (!snapshots.isEmpty()) {
                    if (snapshots.get().getDocumentChanges().size() > 0) {
                        DocumentChange change = snapshots.get().getDocumentChanges().get(0);
                        if (change.getDocument().exists()) {
//                            Sendable sendable = change.getDocument().toObject(Sendable.class);
                            Sendable sendable = sendableFromDocumentSnapshot(change.getDocument());
                            return Optional.with(sendable);
                        }
                    }
                }
                return Optional.empty();
            });
        });
    }

    public static Sendable sendableFromDocumentSnapshot(DocumentSnapshot snapshot) {

        // Get the data
        String id = snapshot.getId();
        String from = snapshot.get(Keys.From, String.class);

        Date date = snapshot.get(Keys.Date, Date.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ;
        String type = snapshot.get(Keys.Type, String.class);

        Map<String, Object> body = (HashMap<String, Object>) snapshot.get(Keys.Body);

        Map<String, Object> data = new HashMap<String, Object>() {{
            put(Keys.From, from);
            put(Keys.Date, date);
            put(Keys.Type, type);
            put(Keys.Body, new Body(body));
        }};

        return new Sendable(id, data);
    }

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    public Observable<Event<Sendable>> messagesOn(Path messagesPath, Date newerThan) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (newerThan != null) {
                query = query.whereGreaterThan(Keys.Date, newerThan);
            }

            emitter.onSuccess(query);
        }).flatMapObservable(query -> new RXFirestore().on(query).flatMapMaybe(change -> {
            DocumentSnapshot ds = change.getDocument();
            if (ds.exists()) {
                Sendable sendable = sendableFromDocumentSnapshot(ds);
//                Sendable sendable = ds.toObject(Sendable.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
//                sendable.setId(ds.getId());

                return Maybe.just(new Event<>(sendable, typeForDocumentChange(change)));
            }
            return Maybe.empty();
        }));
    }

    @Override
    public Object timestamp() {
        return FieldValue.serverTimestamp();
    }

    /**
     * Firestore helper methods
     */

    /**
     * Run a Firestore updateBatch operation
     * @param batch Firestore updateBatch
     * @return completion
     */
    protected Completable runBatch(WriteBatch batch) {
        return Completable.create(emitter -> {
            batch.commit().addOnCompleteListener(task -> {
                emitter.onComplete();
            }).addOnFailureListener(emitter::onError);
        }).subscribeOn(RX.io());
    }

    public static EventType typeForDocumentChange(DocumentChange change) {
        switch (change.getType()) {
            case ADDED:
                return EventType.Added;
            case REMOVED:
                return EventType.Removed;
            case MODIFIED:
                return EventType.Modified;
            default:
                return null;
        }
    }

    public Completable mute(Path path, Map<String, Object> data) {
        return new RXFirestore().set(Ref.document(path), data);
    }

    public Completable unmute(Path path) {
        return new RXFirestore().delete(Ref.document(path));
    }

}
