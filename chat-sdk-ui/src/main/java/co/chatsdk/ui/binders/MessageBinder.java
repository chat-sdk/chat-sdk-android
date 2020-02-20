package co.chatsdk.ui.binders;

import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import co.chatsdk.core.types.MessageSendStatusFormatter;
import co.chatsdk.ui.chat.model.MessageHolder;

public class MessageBinder {

    public static Locale getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static DateFormat messageTimeComparisonDateFormat(Context context) {
        return new SimpleDateFormat("dd-M-yyyy hh:mm", MessageBinder.getCurrentLocale(context));
    }

    public static void onBindSendStatus(TextView textView, MessageHolder holder) {
        String status = MessageSendStatusFormatter.format(textView.getContext(), holder.getStatus(), holder.getUploadPercentage());
        String timeString = status + " " + textView.getText();
        textView.setText(timeString);
    }
}
