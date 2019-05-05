package co.chatsdk.ui.chat.message_action;

import android.app.Activity;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import io.reactivex.Completable;

public class DeleteMessageAction extends MessageAction {

    public DeleteMessageAction(Message message) {
        super(message);
        type = Type.Delete;
        titleResourceId = R.string.delete;
        iconResourceId = R.drawable.ic_delete_white_24dp;
        colorId = R.color.primary;
    }

    @Override
    public Completable execute(Activity activity) {
        return ChatSDK.thread().deleteMessage(message.get());
    }
}
