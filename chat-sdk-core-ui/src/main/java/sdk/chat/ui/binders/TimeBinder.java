package sdk.chat.ui.binders;

import android.view.View;
import android.widget.TextView;

import com.stfalcon.chatkit.utils.DateFormatter;

import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;

public class TimeBinder {

    public void bind(TextView time, MessageHolder message) {
        if (time != null) {
            if (UIModule.config().getMessageTimeFormat() != null) {
                time.setText(DateFormatter.format(message.getCreatedAt(), UIModule.config().getMessageTimeFormat()));
            }
            // Hide the time if it's the same as the next message
            time.setVisibility(message.showDate() ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
