package co.chatsdk.ui.chat.message_action;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;

import com.google.android.material.snackbar.Snackbar;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.ui.R;
import io.reactivex.Completable;

import static android.content.Context.CLIPBOARD_SERVICE;

public class CopyMessageAction extends MessageAction {

    public CopyMessageAction(Message message) {
        super(message);
        type = Type.Copy;
        titleResourceId = R.string.copy;
        iconResourceId = R.drawable.ic_content_copy_white_24dp;
        colorId = R.color.button_success;
        successMessageId = R.string.copied_to_clipboard;
    }

    @Override
    public Completable execute(Activity activity) {
        return Completable.create(emitter -> {
            String text = message.get().getTextRepresentation();
            if (text != null) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(activity.getString(R.string.message), text);
                clipboard.setPrimaryClip(clip);
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(activity.getString(R.string.copy_failed)));
            }
        });
    }
}
