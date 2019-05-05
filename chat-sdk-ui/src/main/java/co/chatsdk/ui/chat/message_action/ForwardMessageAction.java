package co.chatsdk.ui.chat.message_action;

import android.app.Activity;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;

public class ForwardMessageAction extends MessageAction {

    public ForwardMessageAction(Message message) {
        super(message);
        type = Type.Forward;
        titleResourceId = R.string.forward;
        iconResourceId = R.drawable.ic_arrow_forward_white_24dp;
        colorId = R.color.button_success;
    }

    @Override
    public Completable execute(Activity activity) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                ChatSDK.ui().startForwardMessageActivityForResult(activity, message.get());
            }
        };
    }
}
