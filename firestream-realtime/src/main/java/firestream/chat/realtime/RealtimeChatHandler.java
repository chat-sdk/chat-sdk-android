package firestream.chat.realtime;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

import java.util.Date;
import java.util.Map;

import firestream.chat.chat.Meta;
import firestream.chat.chat.User;
import firestream.chat.firebase.service.FirebaseChatHandler;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Paths;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import sdk.guru.realtime.Generic;
import sdk.guru.realtime.RXRealtime;

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
    public Completable setMetaField(String chatId, String key, Object value) {
        return new RXRealtime().set(Ref.get(Paths.chatMetaPath(chatId).child(key)), value);
    }

    @Override
    public Observable<Meta> metaOn(String chatId) {
        return new RXRealtime().on(Ref.get(Paths.chatPath(chatId))).flatMapMaybe(change -> {
            DataSnapshot snapshot = change.getSnapshot();
            if (snapshot.hasChild(Keys.Meta)) {
                snapshot = snapshot.child(Keys.Meta);

                Meta meta = new Meta();

                if (snapshot.hasChild(Keys.Name)) {
                    meta.setName(snapshot.child(Keys.Name).getValue(String.class));
                }
                if (snapshot.hasChild(Keys.Created)) {
                    Long date = snapshot.child(Keys.Created).getValue(Long.class);
                    if (date != null) {
                        meta.setCreated(new Date(date));
                    }
                }
                if (snapshot.hasChild(Keys.ImageURL)) {
                    meta.setImageURL(snapshot.child(Keys.ImageURL).getValue(String.class));
                }
                if (snapshot.hasChild(Keys.Data)) {
                    Map<String, Object> data = snapshot.child(Keys.Data).getValue(Generic.hashMapStringObject());
                    meta.setData(data);
                }
                return Maybe.just(meta);
            }
            return Maybe.empty();
        });
    }

    @Override
    public Single<String> add(Map<String, Object> data, @Nullable Consumer<String> newId) {
        return new RXRealtime().add(Ref.get(Paths.chatsPath()), data, newId);
    }

    @Override
    public Completable delete(String chatId) {
        return new RXRealtime().delete(Ref.get(Paths.chatPath(chatId)));
    }
}
