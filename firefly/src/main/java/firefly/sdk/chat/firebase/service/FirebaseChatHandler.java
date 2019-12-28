package firefly.sdk.chat.firebase.service;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import firefly.sdk.chat.chat.Chat;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import firefly.sdk.chat.chat.User;
import io.reactivex.functions.Consumer;

public abstract class FirebaseChatHandler {

    public abstract Completable leaveChat(String chatId);
    public abstract Completable joinChat(String chatId);
    public abstract Observable<Chat.Meta> metaOn(Path path);

    public abstract Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId);

    public Single<String> add(Path path, HashMap<String, Object> data) {
        return add(path, data, null);
    }

}
