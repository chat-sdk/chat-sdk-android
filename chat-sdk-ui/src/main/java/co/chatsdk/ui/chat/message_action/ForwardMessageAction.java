package co.chatsdk.ui.chat.message_action;

import android.app.Activity;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class ForwardMessageAction extends MessageAction {

    protected int messageForwardActivityCode = 998;

    protected DisposableList disposableList = new DisposableList();

    public ForwardMessageAction(Message message) {
        super(message);
        type = Type.Forward;
        titleResourceId = R.string.forward;
        iconResourceId = R.drawable.ic_arrow_forward_white_24dp;
        colorId = R.color.button_success;
        successMessageId = R.string.message_sent;
    }

    @Override
    public Completable execute(Activity activity) {
        return Completable.create(emitter -> {
            disposableList.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                if (activityResult.requestCode == messageForwardActivityCode) {
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        emitter.onComplete();
                    } else {
                        if (activityResult.data != null) {
                            String errorMessage = activityResult.data.getStringExtra(Keys.IntentKeyErrorMessage);
                            emitter.onError(new Throwable(errorMessage));
                        }
                    }
                    disposableList.dispose();
                }
            }));
            ChatSDK.ui().startForwardMessageActivityForResult(activity, message.get(), messageForwardActivityCode);
        });
    }
}
