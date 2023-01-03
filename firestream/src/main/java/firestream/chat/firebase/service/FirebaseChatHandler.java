package firestream.chat.firebase.service;

import java.util.Map;

import firestream.chat.chat.Meta;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

public abstract class FirebaseChatHandler {

    public abstract Completable leaveChat(String chatId);
    public abstract Completable joinChat(String chatId);

    /**
     * Note in this case, we don't provide the path to the chat/meta
     * we provide it to the chat. This type because of differences between
     * Realtime and Firestore. The realtime database stores the data at
     *  - chat/meta/...
     * But in Firestore meta/... type stored as a field on the chat document
     * So we need to link to the chat document in both cases
     * @param chatId chat room id
     * @return stream of data when chat meta changes
     */
    public abstract Observable<Meta> metaOn(String chatId);

    public abstract Completable setMetaField(String chatId, String key, Object value);

    public abstract Single<String> add(Map<String, Object> data, @Nullable Consumer<String> newId);

    public Single<String> add(Map<String, Object> data) {
        return add(data, null);
    }

    public abstract Completable delete(String chatId);

}
