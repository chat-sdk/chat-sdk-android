package firefly.sdk.chat.firebase.realtime;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;

import firefly.sdk.chat.firebase.generic.GenericTypes;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import firefly.sdk.chat.R;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.firebase.service.FirebaseChatHandler;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.firebase.service.Paths;
import firefly.sdk.chat.namespace.Fl;
import io.reactivex.functions.Function;

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
        return new RXRealtime().on(Ref.get(path)).flatMapMaybe(change -> {
            DataSnapshot snapshot = change.snapshot;
            if (snapshot != null && snapshot.hasChild(Keys.Meta)) {
                HashMap<String, Object> meta = snapshot.child(Keys.Meta).getValue(GenericTypes.meta());
                if (meta != null) {
                    return Maybe.just(meta);
                }
            }
            return Maybe.empty();
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data) {
        return new RXRealtime().add(Ref.get(path), data);
    }
}
