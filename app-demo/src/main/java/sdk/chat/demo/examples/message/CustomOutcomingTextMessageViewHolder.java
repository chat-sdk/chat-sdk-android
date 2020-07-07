package sdk.chat.demo.examples.message;

import android.view.View;

import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.OutcomingTextMessageViewHolder;

public class CustomOutcomingTextMessageViewHolder extends OutcomingTextMessageViewHolder {
    public CustomOutcomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(MessageHolder message) {
        super.onBind(message);
        text.setText(text.getText() + " Hello World");
    }

}
