package firefly.sdk.chat.firebase.firestore;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;

import firefly.sdk.chat.firebase.generic.Generic;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import firefly.sdk.chat.R;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.Paths;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.firebase.service.FirebaseChatHandler;
import firefly.sdk.chat.namespace.Fl;

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
    public Observable<HashMap<String, Object>> metaOn(Path path) {
        return new RXFirestore().on(Ref.document(path)).flatMapMaybe(snapshot -> {
            if (snapshot.getData() != null) {
                HashMap<String, HashMap<String, Object>> data = snapshot.toObject(Generic.UserMetaData.class);
                if (data != null) {
                    HashMap<String, Object> meta = data.get(Keys.Meta);
                    if (meta != null) {
                        return Maybe.just(meta);
                    }
                }
            }
            return Maybe.empty();
        });
    }

    public Single<String> add(Path path, HashMap<String, Object> data) {
        return new RXFirestore().add(Ref.collection(path), data).map(DocumentReference::getId);
    }

}
