package sdk.chat.ui.chat.options;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.MediaSelector;
import io.reactivex.Completable;


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
