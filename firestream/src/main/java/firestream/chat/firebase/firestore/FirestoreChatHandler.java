package firestream.chat.firebase.firestore;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import firestream.chat.chat.Chat;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import firestream.chat.chat.User;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Paths;
import firestream.chat.firebase.service.Path;
import firestream.chat.firebase.service.FirebaseChatHandler;
import io.reactivex.functions.Consumer;

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
    public Completable updateMeta(Path chatPath, HashMap<String, Object> meta) {
        return new RXFirestore().update(Ref.document(chatPath), meta);
    }

    @Override
    public Observable<Chat.Meta> metaOn(Path path) {
        // Remove the last path because in this case, the document ref does not include the "meta keyword"
        return new RXFirestore().on(Ref.document(path)).flatMapMaybe(snapshot -> {
            Chat.Meta meta = new Chat.Meta();

            String base = Keys.Meta + ".";

            meta.name = snapshot.get(base + Keys.Name, String.class);
            meta.created = snapshot.get(base + Keys.Created, Date.class);
            meta.imageURL = snapshot.get(base + Keys.ImageURL, String.class);

            return Maybe.just(meta);
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(path), data, newId);
    }

}
