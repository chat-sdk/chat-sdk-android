package firefly.sdk.chat.firebase.firestore;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;

import javax.annotation.Nullable;

import firefly.sdk.chat.chat.Chat;
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
    public Observable<Chat.Meta> metaOn(Path path) {
        // Remove the last path because in this case, the document ref does not include the "meta keyword"
        return new RXFirestore().on(Ref.document(path)).flatMapMaybe(snapshot -> {
            Chat.Meta meta = new Chat.Meta();

            meta.name = snapshot.get(Keys.Name, String.class);
            meta.created = snapshot.get(Keys.Created, Date.class);
            meta.avatarURL = snapshot.get(Keys.Avatar, String.class);

            return Maybe.just(meta);
        });
    }

    @Override
    public Single<String> add(Path path, HashMap<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXFirestore().add(Ref.collection(path), data, newId);
    }

}
