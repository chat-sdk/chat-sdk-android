package firestream.chat.firebase.firestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import firestream.chat.chat.Meta;
import firestream.chat.firebase.generic.Generic;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import firestream.chat.chat.User;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Paths;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.FirebaseChatHandler;
import io.reactivex.functions.Consumer;

public class FirestoreChatHandler extends FirebaseChatHandler {

    @Override
    public Completable leaveChat(String chatId) {
        return new RXFirestore().delete(Ref.document(Paths.userGroupChatPath(chatId)));
    }

    @Override
    public Completable joinChat(String chatId) {
        return new RXFirestore().set(Ref.document(Paths.userGroupChatPath(chatId)), User.dateDataProvider().data(null));
    }

    @Override
    public Completable updateMeta(Path chatMetaPath, final HashMap<String, Object> meta) {
        chatMetaPath.normalizeForDocument();

        ArrayList<String> keys = new ArrayList<>(meta.keySet());

        HashMap<String, Object> toWrite = meta;
        if (chatMetaPath.getRemainder() != null) {
            toWrite = wrap(chatMetaPath.getRemainder(), meta);
            keys.add(chatMetaPath.getRemainder());
        }
        return new RXFirestore().update(Ref.document(chatMetaPath), toWrite, keys);
    }

    protected HashMap<String, Object> wrap(String key, HashMap<String, Object> map) {
        return new HashMap<String, Object>() {{
            put(key, map);
        }};
    }

    @Override
    public Observable<Meta> metaOn(Path path) {
        return new RXFirestore().on(Ref.document(path)).flatMapMaybe(snapshot -> {
            Meta meta = new Meta();

            String base = Keys.Meta + ".";

            meta.setName(snapshot.get(base + Keys.Name, String.class));
            meta.setCreated(snapshot.get(base + Keys.Created, Date.class));
            meta.setImageURL(snapshot.get(base + Keys.ImageURL, String.class));

            HashMap<String, Object> data = snapshot.get(base + Keys.Data, Generic.HashMapStringObject.class);
            meta.setData(data);

            return Maybe.just(meta);
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(path), data, newId);
    }
}
