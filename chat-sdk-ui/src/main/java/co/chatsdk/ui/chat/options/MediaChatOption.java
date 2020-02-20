package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

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


/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public MediaChatOption(String title, Drawable iconDrawable, final MediaType type) {
        super(title, iconDrawable, null);
        action = (activity, thread) -> new MediaSelector().startActivity(activity, type).flatMapCompletable(files -> {
            ArrayList<Completable> completables = new ArrayList<>();
            for (File file: files) {
                if (type.is(MediaType.Photo)) {
                    completables.add(ChatSDK.imageMessage().sendMessageWithImage(file, thread));
                }
                if (type.is(MediaType.Video)) {
                    completables.add(ChatSDK.videoMessage().sendMessageWithVideo(file, thread));
                }
            }
            return Completable.concat(completables);
        });
    }

    public MediaChatOption(String title, MediaType type) {
        this(title, null, type);
    }
}
