package sdk.chat.ui.binders;

import android.content.Context;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.module.UIModule;

public class MessageBinder {

    public DateFormat messageTimeComparisonDateFormat(Context context) {
        return new SimpleDateFormat("dd-M-yyyy hh:mm", CurrentLocale.get(context));
    }

    public void onBindSendStatus(TextView textView, MessageHolder holder) {

        if (UIModule.config().getMessageTimeFormat() != null) {
            UIModule.shared().getTimeBinder().bind(textView, holder);
//            DateFormat format = new SimpleDateFormat(UIModule.config().messageTimeFormat, CurrentLocale.get());
//            textView.setText(format.format(holder.getCreatedAt()));
        }

        // If the upload failed..
        String status;
        if (holder.canResend()) {
            status = textView.getContext().getString(R.string.failed);
        } else {
            status = format(textView.getContext(), holder.getStatus(), holder.getTransferPercentage(), holder.getFileSize());
        }
        if (holder.getStatus() != MessageSendStatus.Uploading) {
            status += " " + textView.getText();
        }
        textView.setText(status);
    }

    public String format(Context context, MessageSendStatus status, float percentage, float fileSize) {
        switch (status) {
            case Initial:
                return context.getString(R.string.created);
//            case Compressing:
//                return context.getString(R.string.compressing);
//            case WillUpload:
//                return context.getString(R.string.will_upload);
            case Uploading:
                return getUploadingText(context, percentage, fileSize);
//            case DidUpload:
//                return context.getString(R.string.did_upload);
//            case WillSend:
//                return context.getString(R.string.will_send);
            case Sent:
                return ""; // context.getString(R.string.sent);
            case Failed:
                return context.getString(R.string.failed);
            default:
                return "";

        }
    }

    public String getFileSize(float kb) {
        // Depending on the size change the unit
        if (kb < 1000) {
            return String.format("%.0fKB", kb);
        } else {
            double mb = Math.floor(kb / 1000);
            return String.format("%.0fMB", mb);
        }
    }

    public String getUploadingText(Context context, float percentage, float fileSize) {
        String output = "";
        if (percentage >= 0) {
            output = percentage + "%";
        } else {
            output = String.format(context.getString(R.string.uploading__), "");
        }
        if (UIModule.config().showFileSizeDuringUpload && fileSize >= 0) {
            output += " " + getFileSize(fileSize);
        }
        return output;
    }
}
