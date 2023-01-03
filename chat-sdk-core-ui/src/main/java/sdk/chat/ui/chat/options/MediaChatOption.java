package sdk.chat.ui.chat.options;

import android.content.Intent;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.reactivex.Completable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.activities.preview.LassiLauncher;


/**
 * Created by ben on 10/11/17.
 */

public class MediaChatOption extends BaseChatOption {

    public MediaChatOption(@StringRes int title, @DrawableRes int image, final MediaType type) {
        super(title, image, null);
        action = (activity, launcher, thread) -> {
            return Completable.create(emitter -> {
                if (type.is(MediaType.Video)) {
                    Intent intent = LassiLauncher.Companion.launchVideoPicker(activity);
                    ChatSDK.core().addBackgroundDisconnectExemption();
                    launcher.launch(intent);
                }
                if (type.is(MediaType.Photo)) {
                    Intent intent = LassiLauncher.Companion.launchImagePicker(activity);
                    ChatSDK.core().addBackgroundDisconnectExemption();
                    launcher.launch(intent);
                }
            });
        };
    }

}