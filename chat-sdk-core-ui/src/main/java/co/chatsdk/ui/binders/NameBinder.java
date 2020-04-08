package co.chatsdk.ui.binders;

import android.view.View;
import android.widget.TextView;

import co.chatsdk.ui.chat.model.MessageHolder;

public class NameBinder {

    public static void bind(TextView userName, MessageHolder message) {
        if (message.showNames()) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(message.getUser().getName());
        } else {
            userName.setVisibility(View.GONE);
        }
    }

}
