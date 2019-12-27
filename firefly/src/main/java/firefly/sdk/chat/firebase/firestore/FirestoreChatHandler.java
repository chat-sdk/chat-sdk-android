package firefly.sdk.chat.firebase.firestore;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Completable;
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
        return new RXFirestore().on(Ref.document(path)).map(snapshot -> {
            Map<String, Object> map = snapshot.getData();
            if (map != null) {
                Object metaObject = map.get(Keys.Meta);
                if (metaObject instanceof HashMap) {
                    return (HashMap<String, Object>) metaObject;
                }
            }
            throw new Exception(Fl.y.context().getString(R.string.error_null_data));
        });
    }

    public Single<String> add(Path path, HashMap<String, Object> data) {
        return new RXFirestore().add(Ref.collection(path), data).map(DocumentReference::getId);
    }

}
