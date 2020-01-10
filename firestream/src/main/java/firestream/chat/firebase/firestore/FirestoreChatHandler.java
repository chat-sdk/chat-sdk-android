package firestream.chat.firebase.firestore;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.DocumentSnapshot;

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
import io.reactivex.functions.Function;

public class FirestoreChatHandler extends FirebaseChatHandler {

    @Override
    public Completable leaveChat(String chatId) {
        return new RXFirestore().delete(Ref.document(Paths.userGroupChatPath(chatId)));
    }

    @Override
    public Completable joinChat(String chatId) {
        return new RXFirestore().set(Ref.document(Paths.userGroupChatPath(chatId)), User.dateDataProvider().data(null));
    }

    public Completable setMetaField(String chatId, String key, Object value) {
        Path chatMetaPath = Paths.chatMetaPath(chatId);
        chatMetaPath.normalizeForDocument();

        return new RXFirestore().update(Ref.document(chatMetaPath), new HashMap<String, Object>() {{
            put(chatMetaPath.dotPath(key), value);
        }});
    }

    @Override
    public Observable<Meta> metaOn(String chatId) {
        return new RXFirestore().on(Ref.document(Paths.chatPath(chatId))).map(snapshot -> {
            Meta meta = new Meta();

            String base = Keys.Meta + ".";

            meta.setName(snapshot.get(base + Keys.Name, String.class));
            meta.setCreated(snapshot.get(base + Keys.Created, Date.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE));
            meta.setImageURL(snapshot.get(base + Keys.ImageURL, String.class));

            HashMap<String, Object> data = new HashMap<>();

            Object dataObject = snapshot.get(base + Keys.Data);
            if (dataObject instanceof HashMap) {
                HashMap dataMap = (HashMap) dataObject;
                for (Object key: dataMap.keySet()) {
                    if (key instanceof String) {
                        data.put((String) key, dataMap.get(key));
                    }
                }
            }
            meta.setData(data);

            return meta;
        });
    }

    @Override
    public Single<String> add(HashMap<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(Paths.chatsPath()), data, newId);
    }

    @Override
    public Completable delete(String chatId) {
        return new RXFirestore().delete(Ref.document(Paths.chatPath(chatId)));
    }

}
