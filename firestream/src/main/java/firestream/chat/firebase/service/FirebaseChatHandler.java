package firestream.chat.firebase.service;

import java.util.HashMap;

import javax.annotation.Nullable;

import firestream.chat.chat.Chat;
import firestream.chat.chat.Meta;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public abstract class FirebaseChatHandler {

    public abstract Completable leaveChat(String chatId);
    public abstract Completable joinChat(String chatId);

    /**
     * Note in this case, we don't provide the path to the chat/meta
     * we provide it to the chat. This is because of differences between
     * Realtime and Firestore. The realtime database stores the data at
     *  - chat/meta/...
     * But in Firestore meta/... is stored as a field on the chat document
     * So we need to link to the chat document in both cases
     * @param chatPath path to chat document / entity
     * @return stream of data when chat meta changes
     */
    public abstract Observable<Meta> metaOn(Path chatPath);

//    public abstract Completable updateMeta(Path chatMetaPath, HashMap<String, Object> meta);
    public abstract Completable setMetaField(Path chatMetaPath, String key, Object value);

    public abstract Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId);

    public Single<String> add(Path path, HashMap<String, Object> data) {
        return add(path, data, null);
    }

}
