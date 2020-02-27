package co.chatsdk.ui.binders;

import android.view.View;
import android.widget.TextView;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.chat.model.MessageHolder;

public class NameBinder {

    public static void bind(TextView nameTextView, MessageHolder message) {
        if (message.getMessage().getThread().typeIs(ThreadType.Group)) {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(message.getUser().getName());
        } else {
            nameTextView.setVisibility(View.GONE);
        }

    }

}
