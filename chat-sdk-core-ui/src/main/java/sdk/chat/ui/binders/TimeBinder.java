package sdk.chat.ui.binders;

import android.widget.TextView;

import com.stfalcon.chatkit.utils.DateFormatter;

import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;

public class TimeBinder {

    public void bind(TextView time, MessageHolder message) {
        if (UIModule.config().messageTimeFormat != null && time != null) {
            time.setText(DateFormatter.format(message.getCreatedAt(), UIModule.config().messageTimeFormat));
        }
    }
}
