package firefly.sdk.chat.firebase.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import firefly.sdk.chat.R;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.events.EventType;
import firefly.sdk.chat.events.ListEvent;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.FirebaseCoreHandler;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.namespace.Fire;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.SendableType;

public class FirestoreCoreHandler extends FirebaseCoreHandler {

    @Override
    public Observable<ListEvent> listChangeOn(Path path) {
        return new RXFirestore().on(Ref.collection(path)).flatMapMaybe(change -> {
            DocumentSnapshot d = change.getDocument();
            if (d.exists()) {
                EventType type = FirestoreCoreHandler.typeForDocumentChange(change);
                if (type != null) {
                    return Maybe.just(new ListEvent(d.getId(), d.getData(), type));
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
    public Single<String> send(Path messagesPath, Sendable sendable) {
        return new RXFirestore().add(Ref.collection(messagesPath), sendable.toData()).map(DocumentReference::getId);
    }

    @Override
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.id);
                batch.set(docRef, dataProvider.data(u));
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.id);
                batch.update(docRef, dataProvider.data(u));
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Completable removeUsers(Path path, List<User> users) {
        return Single.create((SingleOnSubscribe<WriteBatch>) emitter -> {
            CollectionReference ref = Ref.collection(path);
            WriteBatch batch = Ref.db().batch();

            for (User u : users) {
                DocumentReference docRef = ref.document(u.id);
                batch.delete(docRef);
            }
            emitter.onSuccess(batch);
        }).flatMapCompletable(this::runBatch);
    }

    @Override
    public Observable<Sendable> messagesOnce(Path messagesPath, @Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (fromDate != null) {
                query = query.whereGreaterThan(Keys.Date, fromDate);
            }
            if (toDate != null) {
                query = query.whereLessThan(Keys.Date, toDate);
            }
            if (limit != null) {
                query = query.limit(limit);
            }

            emitter.onSuccess(query);
        }).flatMap(query -> new RXFirestore().get(query)).flatMapObservable(snapshots -> Observable.create(emitter -> {
            if (!snapshots.isEmpty()) {
                for (DocumentChange c : snapshots.get().getDocumentChanges()) {
                    DocumentSnapshot s = c.getDocument();
                    // Add the message
                    if (s.exists() && c.getType() == DocumentChange.Type.ADDED) {
                        Sendable sendable = s.toObject(Sendable.class);
                        sendable.id = s.getId();
                        emitter.onNext(sendable);
                    }
                }
            }
            emitter.onComplete();
        }));
    }

    @Override
    public Single<Date> dateOfLastSentMessage(Path messagesPath) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.whereEqualTo(Keys.From, Fl.y.currentUserId());
            query = query.orderBy(Keys.Date, Query.Direction.DESCENDING);
            query = query.limit(1);

            emitter.onSuccess(query);
        }).flatMap(query -> new RXFirestore().get(query).map(snapshots -> {
            if (!snapshots.isEmpty()) {
                if (snapshots.get().getDocumentChanges().size() > 0) {
                    DocumentChange change = snapshots.get().getDocumentChanges().get(0);
                    if (change.getDocument().exists()) {
                        Sendable sendable = change.getDocument().toObject(Sendable.class);
                        return sendable.getDate();
                    }
                }
            }
            return new Date(0);
        }));
    }

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    public Observable<Sendable> messagesOn(Path messagesPath, Date newerThan, int limit) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.orderBy(Keys.Date, Query.Direction.ASCENDING);
            if (newerThan != null) {
                query = query.whereGreaterThan(Keys.Date, newerThan);
            }
            query.limit(limit);

            emitter.onSuccess(query);
        }).flatMapObservable(query -> new RXFirestore().on(query).flatMapMaybe(change -> {
            DocumentSnapshot ds = change.getDocument();
            if (change.getType() == DocumentChange.Type.ADDED) {
                if (ds.exists()) {
                    Sendable sendable = ds.toObject(Sendable.class);
                    sendable.id = ds.getId();
                    return Maybe.just(sendable);
                }
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
        });
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

}
