package firefly.sdk.chat.firebase.service;

import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import firefly.sdk.chat.chat.User;

public abstract class FirebaseChatHandler {

    public abstract Completable leaveChat(String chatId);
    public abstract Completable joinChat(String chatId);
    public abstract Observable<HashMap<String, Object>> metaOn(Path path);
    public abstract Single<String> add(Path path, HashMap<String, Object> data);

}
