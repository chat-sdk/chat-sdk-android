package sdk.chat.micro.firebase.realtime;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import sdk.chat.micro.R;
import sdk.chat.micro.chat.User;
import sdk.chat.micro.firebase.firestore.RXFirestore;
import sdk.chat.micro.firebase.service.FirebaseChatHandler;
import sdk.chat.micro.firebase.service.Keys;
import sdk.chat.micro.firebase.service.Path;
import sdk.chat.micro.firebase.service.Paths;
import sdk.chat.micro.namespace.Fly;

public class RealtimeChatHandler extends FirebaseChatHandler {

    @Override
    public Completable leaveChat(String chatId) {
        return new RXRealtime().delete(Ref.get(Paths.userGroupChatPath(chatId)));
    }

    @Override
    public Completable joinChat(String chatId) {
        return new RXRealtime().set(Ref.get(Paths.userGroupChatPath(chatId)), User.dateDataProvider().data(null));
    }

    @Override
    public Observable<HashMap<String, Object>> metaOn(Path path) {
        return new RXRealtime().on(Ref.get(path)).map(change -> {
            DataSnapshot snapshot = change.snapshot;
            if (snapshot != null && snapshot.hasChild(Keys.Meta)) {
                return snapshot.child(Keys.Meta).getValue(HashMap.class);
            }
            throw new Exception(Fly.y.context().getString(R.string.error_null_data));
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data) {
        return new RXRealtime().add(Ref.get(path), data);
    }
}
