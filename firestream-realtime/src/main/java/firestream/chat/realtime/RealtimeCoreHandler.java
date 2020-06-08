package firestream.chat.realtime;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import firestream.chat.chat.User;
import firestream.chat.events.ListData;
import firestream.chat.firebase.service.FirebaseCoreHandler;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.message.Body;
import firestream.chat.message.Sendable;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import sdk.guru.common.Event;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;
import sdk.guru.realtime.DocumentChange;
import sdk.guru.realtime.Generic;
import sdk.guru.realtime.RXRealtime;

public class RealtimeCoreHandler extends FirebaseCoreHandler {

    @Override
    public Observable<Event<ListData>> listChangeOn(Path path) {
        return new RXRealtime().childOn(Ref.get(path)).flatMapMaybe((Function<DocumentChange, MaybeSource<Event<ListData>>>) change -> {
            DataSnapshot snapshot = change.getSnapshot();
            Map<String, Object> data = snapshot.getValue(Generic.hashMapStringObject());
            if (data != null) {
                return Maybe.just(new Event<>(new ListData(change.getSnapshot().getKey(), data), change.getType()));
            }
            return Maybe.empty();
        });
    }

    @Override
    public Completable deleteSendable(Path messagesPath) {
        return new RXRealtime().delete(Ref.get(messagesPath));
    }

    @Override
    public Completable send(Path messagesPath, Sendable sendable, Consumer<String> newId) {
        return new RXRealtime().add(Ref.get(messagesPath), sendable.toData(), timestamp(), newId).ignoreElement();
    }

    @Override
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return addBatch(path, idsForUsers(users), dataForUsers(users, dataProvider));
    }

    @Override
    public Completable removeUsers(Path path, List<? extends User> users) {
        return removeBatch(path, idsForUsers(users));
    }

    @Override
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return updateBatch(path, idsForUsers(users), dataForUsers(users, dataProvider));
    }

    @Override
    public Single<List<Sendable>> loadMoreMessages(Path messagesPath, @Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
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
                if (fromDate != null) {
                    query = query.limitToFirst(limit);
                }
                if (toDate != null) {
                    query = query.limitToLast(limit);
                }
            }

            emitter.onSuccess(query);
        }).flatMap(query -> new RXRealtime().get(query)).map(optional -> {
            List<Sendable> sendables = new ArrayList<>();
            if (!optional.isEmpty()) {
                DataSnapshot snapshot = optional.get();
                if (snapshot.exists()) {
                    for (DataSnapshot child: snapshot.getChildren()) {
                        Sendable sendable = sendableFromSnapshot(child);
                        if (sendable != null) {
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
            Query query = Ref.get(messagesPath);

            query = query.orderByChild(Keys.Date);
            query = query.limitToLast(1);

            return new RXRealtime().get(query).map(optional -> {
                if (!optional.isEmpty()) {
                    for (DataSnapshot snapshot: optional.get().getChildren()) {
                        Sendable sendable = sendableFromSnapshot(snapshot);
                        return Optional.with(sendable);
                    }
                }
                return Optional.empty();
            });
        });
    }

    public Sendable sendableFromSnapshot(DataSnapshot snapshot) {

        Sendable sendable = new Sendable();
        sendable.setId(snapshot.getKey());

        if (snapshot.hasChild(Keys.From)) {
            sendable.setFrom(snapshot.child(Keys.From).getValue(String.class));
        }
        if (snapshot.hasChild(Keys.Date)) {
            Long timestamp = snapshot.child(Keys.Date).getValue(Long.class);
            if (timestamp != null) {
                sendable.setDate(new Date(timestamp));
            }
        }
        if (snapshot.hasChild(Keys.Type)) {
            sendable.setType(snapshot.child(Keys.Type).getValue(String.class));
        }
        if (snapshot.hasChild(Keys.Body)) {
            Map<String, Object> body = snapshot.child(Keys.Body).getValue(Generic.hashMapStringObject());
            if (body != null) {
                sendable.setBody(new Body(body));
            }
        }
        return sendable;

    }

    @Override
    public Observable<Event<Sendable>> messagesOn(Path messagesPath, Date newerThan) {
        return Single.create((SingleOnSubscribe<Query>) emitter -> {
            Query query = Ref.get(messagesPath);

            query = query.orderByChild(Keys.Date);
            if (newerThan != null) {
                query = query.startAt(newerThan.getTime(), Keys.Date);
            }
            emitter.onSuccess(query);
        }).flatMapObservable(query -> new RXRealtime().childOn(query).flatMapMaybe(change -> {
            Sendable sendable = sendableFromSnapshot(change.getSnapshot());
            if (sendable != null) {
                return Maybe.just(new Event<>(sendable, change.getType()));
            }
            return Maybe.empty();
        }));
    }

    @Override
    public Object timestamp() {
        return ServerValue.TIMESTAMP;
    }

    protected Completable removeBatch(Path path, List<String> keys) {
        return updateBatch(path, keys, null);
    }

    protected Completable addBatch(Path path, List<String> keys, @Nullable List<Map<String, Object>> values) {
        return updateBatch(path, keys, values);
    }

    protected Completable updateBatch(Path path, List<String> keys, @Nullable List<Map<String, Object>> values) {
        return Completable.create(emitter -> {

            Map<String, Object> data = new HashMap<>();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                Map<String, Object> value = values != null ? values.get(i) : null;
                data.put(path.toString() + "/" + key, value);
            }

           Task<Void> task = Ref.db().getReference().updateChildren(data);
            task.addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        }).subscribeOn(RX.io());
    }

    protected List<String> idsForUsers(List<? extends User> users) {
        List<String> ids = new ArrayList<>();
        for (User u: users) {
            ids.add(u.getId());
        }
        return ids;
    }

    protected List<Map<String, Object>> dataForUsers(List<? extends User> users, User.DataProvider provider) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (User u: users) {
            data.add(provider.data(u));
        }
        return data;
    }

    public Completable mute(Path path, Map<String, Object> data) {
        return new RXRealtime().set(Ref.get(path), data);
    }

    public Completable unmute(Path path) {
        return new RXRealtime().delete(Ref.get(path));
    }

}
