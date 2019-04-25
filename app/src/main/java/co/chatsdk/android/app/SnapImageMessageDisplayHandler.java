package co.chatsdk.android.app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.chat.handlers.AbstractMessageDisplayHandler;
import co.chatsdk.ui.chat.viewholder.ImageMessageViewHolder;

public class SnapImageMessageDisplayHandler extends AbstractMessageDisplayHandler {
    @Override
    public void updateMessageCellView(Message message, AbstractMessageViewHolder viewHolder, Context context) {

    }

    @Override
    public String displayName(Message message) {
        return ChatSDK.shared().context().getString(co.chatsdk.ui.R.string.image_message);
    }

    @Override
    public AbstractMessageViewHolder newViewHolder(boolean isReply, Activity activity) {
        View row = row(isReply, activity);
        return new SnapImageMessageViewHolder(row, activity);
    }

    @Override
    protected View row (boolean isReply, Activity activity) {
        View row;
        LayoutInflater inflater = LayoutInflater.from(activity);
        if(isReply) {
            row = inflater.inflate(R.layout.chat_sdk_row_snap_message_incoming, null);
        } else {
            row = inflater.inflate(R.layout.chat_sdk_row_snap_message_sending, null);
        }
        return row;
    }
}
