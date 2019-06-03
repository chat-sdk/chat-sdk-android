package co.chatsdk.ui.chat.message_action;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.google.android.material.snackbar.Snackbar;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.chat.handlers.AbstractMessageDisplayHandler;
import co.chatsdk.ui.login.SplashScreenActivity;
import co.chatsdk.ui.threads.ThreadsFragment;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.Single;

import static android.content.Context.CLIPBOARD_SERVICE;

public class SaveMessageAction extends MessageAction {

    public SaveMessageAction(Message message) {
        super(message);
        type = Type.Save;
        titleResourceId = R.string.save;
        iconResourceId = R.drawable.ic_56_save;
        colorId = R.color.dark_text;
        successMessageId = R.string.image_saved_successfully;
    }

    @Override
    public Completable execute(Activity activity) {
        return Completable.create(emitter -> {
            String messageImageURL = message.get().stringForKey(Keys.MessageImageURL);
            if (messageImageURL != null) {
                Single<Bitmap> bitmap = ImageBuilder.bitmapForURL(activity, messageImageURL);
                Bitmap bitmap2 = bitmap.blockingGet();
                PermissionRequestHandler.shared().requestWriteExternalStorage(activity).subscribe(() -> {
                    if (bitmap != null) {
                        String bitmapURL = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap2, "", "");
                        if (bitmapURL != null) {
                            ToastHelper.show(activity, activity.getString(R.string.image_saved_successfully));
                        }
                        else {
                            ToastHelper.show(activity, activity.getString(co.chatsdk.ui.R.string.image_save_failed));
                        }
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable(activity.getString(R.string.save_failed)));
                    }
                });
            }
        });
    }
}