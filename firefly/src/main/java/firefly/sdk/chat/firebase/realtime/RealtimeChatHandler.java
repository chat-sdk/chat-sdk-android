package firefly.sdk.chat.firebase.realtime;

import com.google.firebase.database.DataSnapshot;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import firefly.sdk.chat.chat.Chat;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import firefly.sdk.chat.chat.User;
import firefly.sdk.chat.firebase.service.FirebaseChatHandler;
import firefly.sdk.chat.firebase.service.Keys;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.firebase.service.Paths;
import io.reactivex.functions.Consumer;

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
    public Observable<Chat.Meta> metaOn(Path path) {
        return new RXRealtime().on(Ref.get(path)).flatMapMaybe(change -> {
            DataSnapshot snapshot = change.snapshot;
            if (snapshot.hasChild(Keys.Meta)) {
                snapshot = snapshot.child(Keys.Meta);

                Chat.Meta meta = new Chat.Meta();

                if (snapshot.hasChild(Keys.Name)) {
                    meta.name = snapshot.child(Keys.Name).getValue(String.class);
                }
                if (snapshot.hasChild(Keys.Created)) {
                    Long date = snapshot.child(Keys.Created).getValue(Long.class);
                    if (date != null) {
                        meta.created = new Date(date);
                    }
                }
                if (snapshot.hasChild(Keys.Avatar)) {
                    meta.avatarURL = snapshot.child(Keys.Avatar).getValue(String.class);
                }
                return Maybe.just(meta);
            }
            return Maybe.empty();
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXRealtime().add(Ref.get(path), data, newId);
    }
}
