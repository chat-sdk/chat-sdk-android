package co.chatsdk.message.sticker.integration;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.core.utils.StringChecker;
import co.chatsdk.message.sticker.R;
import co.chatsdk.message.sticker.view.StickerMessageActivity;
import co.chatsdk.ui.chat.options.BaseChatOption;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.guru.common.DisposableMap;

import static android.app.Activity.RESULT_OK;
import static co.chatsdk.message.sticker.module.StickerMessageModule.CHOOSE_STICKER;

/**
 * Created by ben on 10/12/17.
 */

public class StickerChatOption extends BaseChatOption {

    protected DisposableMap disposableList = new DisposableMap();

    public StickerChatOption(String title, Drawable resourceDrawable) {
        super(title, resourceDrawable, null);
        action = (activity, thread) -> Single.create((SingleOnSubscribe<String>) emitter -> {

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

    public StickerChatOption(String title) {
        this(title, null);
    }

}
