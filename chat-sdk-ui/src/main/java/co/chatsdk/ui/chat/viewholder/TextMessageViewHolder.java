package co.chatsdk.ui.chat.viewholder;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.view.View;

import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.chat.BaseMessageViewHolder;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class TextMessageViewHolder extends BaseMessageViewHolder {

    public TextMessageViewHolder(View itemView, Activity activity, PublishSubject<List<MessageAction>> actionPublishSubject) {
        super(itemView, activity, actionPublishSubject);
    }

    @Override
    public void setMessage(Message message) {
        super.setMessage(message);

        messageTextView.setText(message.getText() == null ? "" : message.getText());
        setBubbleHidden(false);
        setTextHidden(false);

//        Timber.d("Is Me: " + message.getSender().isMe() + " left padding: " + messageBubble.getPaddingLeft() + " right padding: " + messageBubble.getPaddingRight());

        if (message.getSender().isMe()) {
            messageTextView.setTextColor(ChatSDK.config().messageTextColorMe);
        }
        else {
            messageTextView.setTextColor(ChatSDK.config().messageTextColorReply);
        }

//        messageTextView.setText("HelloHelloHelloHel34523_loHelloHelloHelloHelloHelloHelloHelloHelloHello.png");
//        setIconHidden(false);

    }

    @Override
    public boolean onLongClick(View v) {
        return super.onLongClick(v);




    }
}