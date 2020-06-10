package sdk.chat.core.types;

import android.content.Context;

import sdk.chat.core.R;

public class MessageSendStatusFormatter {
    public static String format(Context context, MessageSendStatus status, Integer percentage) {
        switch (status) {
            case Created:
                return context.getString(R.string.created);
            case Compressing:
                return context.getString(R.string.compressing);
            case WillUpload:
                return context.getString(R.string.will_upload);
            case Uploading:
                if (percentage != null) {
                    return String.format(context.getString(R.string.uploading__), " " + percentage + "%");
                } else {
                    return String.format(context.getString(R.string.uploading__), "");
                }
            case DidUpload:
                return context.getString(R.string.did_upload);
            case WillSend:
                return context.getString(R.string.will_send);
            case Sent:
                return context.getString(R.string.sent);
            case Failed:
                return context.getString(R.string.failed);
            default:
                return "";

        }
    }
}
