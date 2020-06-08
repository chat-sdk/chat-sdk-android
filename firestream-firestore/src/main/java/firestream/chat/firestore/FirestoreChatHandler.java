package firestream.chat.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import firestream.chat.chat.Meta;
import firestream.chat.chat.User;
import firestream.chat.firebase.service.FirebaseChatHandler;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.Paths;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import sdk.guru.firestore.RXFirestore;

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

            Map<String, Object> data = new HashMap<>();

            Object dataObject = snapshot.get(base + Keys.Data);
            if (dataObject instanceof HashMap) {
                Map dataMap = (HashMap) dataObject;
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
    public Single<String> add(Map<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(Paths.chatsPath()), data, newId);
    }

    @Override
    public Completable delete(String chatId) {
        return new RXFirestore().delete(Ref.document(Paths.chatPath(chatId)));
    }

}
