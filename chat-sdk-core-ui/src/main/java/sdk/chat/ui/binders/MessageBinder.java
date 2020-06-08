package sdk.chat.ui.binders;

import android.content.Context;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import sdk.chat.core.types.MessageSendStatusFormatter;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.DefaultUIModule;

public class MessageBinder {

    public static DateFormat messageTimeComparisonDateFormat(Context context) {
        return new SimpleDateFormat("dd-M-yyyy hh:mm", CurrentLocale.get(context));
    }

    public static void onBindSendStatus(TextView textView, MessageHolder holder) {

        if (DefaultUIModule.config().dateFormat != null) {
            DateFormat format = new SimpleDateFormat(DefaultUIModule.config().dateFormat, CurrentLocale.get());
            textView.setText(format.format(holder.getCreatedAt()));
        }

        String status = MessageSendStatusFormatter.format(textView.getContext(), holder.getStatus(), holder.getUploadPercentage());
        String timeString = status + " " + textView.getText();
        textView.setText(timeString);
    }
}
