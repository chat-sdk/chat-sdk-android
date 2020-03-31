package co.chatsdk.message.sticker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.chat.options.BaseChatOption;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import static android.app.Activity.RESULT_OK;
import static co.chatsdk.message.sticker.StickerMessageModule.CHOOSE_STICKER;

/**
 * Created by ben on 10/12/17.
 */

public class StickerChatOption extends BaseChatOption {

    protected DisposableList disposableList = new DisposableList();

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
