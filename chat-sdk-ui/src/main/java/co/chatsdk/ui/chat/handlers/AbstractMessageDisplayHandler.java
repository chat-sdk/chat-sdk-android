package co.chatsdk.ui.chat.handlers;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.ui.R;

public abstract class AbstractMessageDisplayHandler implements MessageDisplayHandler {

    protected View row (boolean isReply, Activity activity) {
        View row;
        LayoutInflater inflater = LayoutInflater.from(activity);
        if(isReply) {
            row = inflater.inflate(R.layout.view_message_reply, null);
        } else {
            row = inflater.inflate(R.layout.view_message_me, null);
        }
        return row;
    }
}
