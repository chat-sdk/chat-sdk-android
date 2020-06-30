package sdk.chat.ui.binders;

import android.view.View;
import android.widget.TextView;

import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIConfig;
import sdk.chat.ui.module.UIModule;

public class NameBinder {

    public void bind(TextView userName, MessageHolder message) {
        if (message.showNames()) {
            userName.setVisibility(View.VISIBLE);
            userName.setText(message.getUser().getName());
        } else {
            userName.setVisibility(View.GONE);
        }
    }

}
