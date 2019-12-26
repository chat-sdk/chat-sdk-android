package sdk.chat.micro.firebase.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import sdk.chat.micro.R;
import sdk.chat.micro.chat.User;
import sdk.chat.micro.events.Event;
import sdk.chat.micro.events.EventType;
import sdk.chat.micro.events.ListEvent;
import sdk.chat.micro.firebase.service.Keys;
import sdk.chat.micro.firebase.service.Paths;
import sdk.chat.micro.firebase.service.FirebaseCoreHandler;
import sdk.chat.micro.firebase.service.Path;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.namespace.Fly;
import sdk.chat.micro.types.SendableType;

public class FirestoreCoreHandler extends FirebaseCoreHandler {

    @Override
    public Observable<ListEvent> listChangeOn(Path path) {
        return new RXFirestore().on(Ref.collection(path)).map(change -> {
            DocumentSnapshot d = change.getDocument();
            if (d.exists()) {
                EventType type = FirestoreCoreHandler.typeForDocumentChange(change);
                return new ListEvent(d.getId(), d.getData(), type);
            }
            throw new Exception(Fly.y.context().getString(R.string.error_null_data));
        });
    }

    @Override
    public Completable deleteSendable (Path messagesPath) {
        return new RXFirestore().delete(Ref.document(messagesPath));
    }

    @Override
    public Single<String> send(Path messagesPath, Sendable sendable) {
        return new RXFirestore().add(Ref.collection(messagesPath), sendable).map(DocumentReference::getId);
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
            if (snapshots != null) {
                for (DocumentChange c : snapshots.getDocumentChanges()) {
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
    public Single<Date> dateOfLastDeliveryReceipt(Path messagesPath) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.collection(messagesPath);

            query = query.whereEqualTo(Keys.Type, SendableType.DeliveryReceipt);
            query = query.whereEqualTo(Keys.From, Fly.y.currentUserId());
            query = query.orderBy(Keys.Date, Query.Direction.DESCENDING);
            query = query.limit(1);
            emitter.onSuccess(query);

        }).flatMap((Function<Query, SingleSource<Date>>) query -> new RXFirestore().get(query).map(snapshots -> {
           if (snapshots.getDocumentChanges().size() > 0) {
                DocumentChange change = snapshots.getDocumentChanges().get(0);
                if (change.getDocument().exists()) {
                    Sendable sendable = change.getDocument().toObject(Sendable.class);
                    return sendable.getDate();
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
        }).flatMapObservable(query -> new RXFirestore().on(query).map(change -> {
            DocumentSnapshot ds = change.getDocument();
            if (change.getType() == DocumentChange.Type.ADDED) {
                if (ds.exists()) {
                    Sendable sendable = ds.toObject(Sendable.class);
                    sendable.id = ds.getId();
                    return sendable;
                }
            }
            return null;
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
     * Run a Firestore batch operation
     * @param batch Firestore batch
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
