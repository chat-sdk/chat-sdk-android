package firefly.sdk.chat.firebase.realtime;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import firefly.sdk.chat.R;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.events.EventType;
import firefly.sdk.chat.events.ListEvent;
import firefly.sdk.chat.firebase.service.FirebaseCoreHandler;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.SendableType;

public class RealtimeCoreHandler extends FirebaseCoreHandler {

    @Override
    public Observable<ListEvent> listChangeOn(Path path) {
        return new RXRealtime().on(Ref.get(path)).map(change -> {
            DataSnapshot snapshot = change.snapshot;
            HashMap<String, Object> data = snapshot.getValue (HashMap.class);
            if (data != null) {
                return new ListEvent(change.snapshot.getKey(), data, change.type);
            }
            throw new Exception(Fl.y.context().getString(R.string.error_null_data));
        });
    }

    @Override
    public Completable deleteSendable(Path messagesPath) {
        return new RXRealtime().delete(Ref.get(messagesPath));
    }

    @Override
    public Single<String> send(Path messagesPath, Sendable sendable) {
        return new RXRealtime().add(Ref.get(messagesPath), sendable);
    }

    @Override
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return addBatch(path, idsForUsers(users), dataForUsers(users, dataProvider));
    }

    @Override
    public Completable removeUsers(Path path, List<User> users) {
        return removeBatch(path, idsForUsers(users));
    }

    @Override
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return updateBatch(path, idsForUsers(users), dataForUsers(users, dataProvider));
    }

    @Override
    public Observable<Sendable> messagesOnce(Path messagesPath, @Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.get(messagesPath);
            query = query.orderByChild(Keys.Date);

            if (fromDate != null) {
                query = query.startAt(fromDate.getTime(), Keys.Date);
            }

            if(toDate != null) {
                query = query.endAt(toDate.getTime(), Keys.Date);
            }

            if (limit != null) {
                // TODO: Check this...
                query = query.limitToLast(limit);
            }

            emitter.onSuccess(query);
        }).flatMap(query -> new RXRealtime().get(query)).flatMapObservable(snapshot -> {
            System.out.println("TestThis");

            return null;
        });
    }

    @Override
    public Single<Date> dateOfLastDeliveryReceipt(Path messagesPath) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {

            Query query = Ref.get(messagesPath);

            query = query.equalTo(SendableType.DeliveryReceipt, Keys.Type);
            query = query.equalTo(Fl.y.currentUserId(), Keys.From);
            query = query.orderByChild(Keys.Date);
            query = query.limitToFirst(1);

            emitter.onSuccess(query);
        }).flatMap(query -> new RXRealtime().get(query).map(snapshot -> {
            Sendable sendable = sendableFromSnapshot(snapshot);
            return sendable.getDate();
        }));
    }

    public Sendable sendableFromSnapshot(DataSnapshot snapshot) {
        if (snapshot != null && snapshot.exists()) {

            Sendable sendable = new Sendable();
            sendable.id = snapshot.getKey();

            if (snapshot.hasChild(Keys.Body)) {
                sendable.from = snapshot.child(Keys.Body).getValue(String.class);
            }
            if (snapshot.hasChild(Keys.Date)) {
                sendable.date = snapshot.child(Keys.Date).getValue(Date.class);
            }
            if (snapshot.hasChild(Keys.Type)) {
                sendable.type = snapshot.child(Keys.Type).getValue(String.class);
            }
            if (snapshot.hasChild(Keys.Body)) {
                sendable.body = snapshot.child(Keys.Body).getValue(HashMap.class);
            }

            return sendable;
        }
        return null;
    }

    @Override
    public Observable<Sendable> messagesOn(Path messagesPath, Date newerThan, int limit) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.get(messagesPath);

            query = query.orderByChild(Keys.Date);
            if (newerThan != null) {
                query = query.startAt(newerThan.getTime(), Keys.Date);
            }
            query = query.limitToLast(limit);
            emitter.onSuccess(query);
        }).flatMapObservable((Function<Query, ObservableSource<Sendable>>) query -> new RXRealtime().on(query).map(change -> {
            if (change.type == EventType.Added) {
                return sendableFromSnapshot(change.snapshot);
            }
            return null;
        }));
    }

    @Override
    public Object timestamp() {
        return ServerValue.TIMESTAMP;
    }

    protected Completable removeBatch(Path path, List<String> keys) {
        return updateBatch(path, keys, null);
    }

    protected Completable addBatch(Path path, List<String> keys, @Nullable List<HashMap<String, Object>> values) {
        return updateBatch(path, keys, values);
    }

    protected Completable updateBatch(Path path, List<String> keys, @Nullable List<HashMap<String, Object>> values) {
        return Completable.create(emitter -> {

            HashMap<String, Object> data = new HashMap<>();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                HashMap<String, Object> value = values != null ? values.get(i) : null;
                data.put(path.toString() + "/" + key, value);
            }

           Task<Void> task = Ref.db().getReference().updateChildren(data);
            task.addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        });
    }

    protected List<String> idsForUsers(List<User> users) {
        ArrayList<String> ids = new ArrayList<>();
        for (User u: users) {
            ids.add(u.id);
        }
        return ids;
    }

    protected List<HashMap<String, Object>> dataForUsers(List<User> users, User.DataProvider provider) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        for (User u: users) {
            data.add(provider.data(u));
        }
        return data;
    }

}
