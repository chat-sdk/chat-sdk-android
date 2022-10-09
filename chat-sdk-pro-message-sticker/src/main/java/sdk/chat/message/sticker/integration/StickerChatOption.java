package sdk.chat.message.sticker.integration;

import static android.app.Activity.RESULT_OK;
import static sdk.chat.message.sticker.module.StickerMessageModule.CHOOSE_STICKER;

import android.content.Intent;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.view.StickerMessageActivity;
import sdk.chat.ui.chat.options.BaseChatOption;
import sdk.guru.common.DisposableMap;

/**
 * Created by ben on 10/12/17.
 */

public class StickerChatOption extends BaseChatOption {

    protected DisposableMap disposableList = new DisposableMap();

    public StickerChatOption(@StringRes int title, @DrawableRes int image) {
        super(title, image, null);
        action = (activity, launcher, thread) -> Single.create((SingleOnSubscribe<String>) emitter -> {

            disposableList.dispose();
            // Listen for the context result which is when the sticker context
            // finishes
            disposableList.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {

                // If the result is ok, connect the message send observable to the returned
                // Observable
                if (activityResult.requestCode == CHOOSE_STICKER && activityResult.resultCode == RESULT_OK) {
                    // Get the sticker name
                    String stickerName = activityResult.data.getStringExtra(Keys.MessageStickerName);
                    if (!StringChecker.isNullOrEmpty(stickerName)) {
                        emitter.onSuccess(stickerName);
                    }
                    else {
                        emitter.onError(new Throwable(activity.getString(R.string.sticker_send_error)));
                    }
                }
            }));

            // Start the sticker context
            Intent intent = new Intent(activity, StickerMessageActivity.class);
            activity.startActivityForResult(intent, CHOOSE_STICKER);

        }).flatMapCompletable(s -> ChatSDK.stickerMessage().sendMessageWithSticker(s, thread));
    }

    @Override
    public AbstractKeyboardOverlayFragment getOverlay(KeyboardOverlayHandler sender) {
        return ChatSDK.stickerMessage().keyboardOverlay(sender);
    }

    @Override
    public boolean hasOverlay() {
        return true;
    }

}
