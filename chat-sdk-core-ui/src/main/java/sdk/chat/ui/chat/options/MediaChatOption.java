package sdk.chat.ui.chat.options;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Completable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.MediaSelector;


/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public MediaChatOption(String title, Drawable iconDrawable, final MediaType type, MediaSelector.CropType cropType) {
        super(title, iconDrawable, null);
        action = (activity, thread) -> new MediaSelector().startActivity(activity, type, cropType).flatMapCompletable(files -> {
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

    public MediaChatOption(String title, MediaType type, MediaSelector.CropType cropType) {
        this(title, null, type, cropType);
    }

    public MediaChatOption(String title, MediaType type) {
        this(title, null, type, MediaSelector.CropType.Rectangle);
    }

    public MediaChatOption(String title, Drawable iconDrawable, final MediaType type) {
        this(title, iconDrawable, type, MediaSelector.CropType.Rectangle);
    }
}