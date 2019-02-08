package co.chatsdk.ui.chat.viewholder;

import android.app.Activity;
import android.view.View;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.chat.BaseMessageViewHolder;

public class TextMessageViewHolder extends BaseMessageViewHolder {

    public TextMessageViewHolder(View itemView, Activity activity) {
        super(itemView, activity);
    }

    @Override
    public void setMessage(Message message) {
        super.setMessage(message);

        messageTextView.setText(message.getText() == null ? "" : message.getText());
        setBubbleHidden(false);
        setTextHidden(false);

//        messageTextView.setText("HelloHelloHelloHel34523_loHelloHelloHelloHelloHelloHelloHelloHelloHello.png");
//        setIconHidden(false);

    }
}