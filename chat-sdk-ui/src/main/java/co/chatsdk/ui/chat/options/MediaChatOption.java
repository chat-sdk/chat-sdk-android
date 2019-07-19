package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.widget.Toast;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public MediaChatOption(String title, Integer iconResourceId, final MediaType type) {
        super(title, iconResourceId, null);
        action = (activity, thread) -> new MediaSelector().startActivity(activity, type).flatMapCompletable(path -> {
            if (type.is(MediaType.Photo)) {
                return ChatSDK.imageMessage().sendMessageWithImage(path, thread);
            }
            if (type.is(MediaType.Video)) {
                return ChatSDK.videoMessage().sendMessageWithVideo(path, thread);
            }
            return Completable.complete();
        });
    }

    public MediaChatOption(String title, MediaType type) {
        this(title, null, type);
    }
}
