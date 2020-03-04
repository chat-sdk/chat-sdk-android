package co.chatsdk.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.dao.Message;
import co.chatsdk.ui.chat.model.MessageHolder;

/**
 * Use this class to customize messages and their click behaviour
 */

public interface IMessageHandler extends MessageHolders.ContentChecker<MessageHolder> {

    void onBindMessageHolders(Context context, MessageHolders holders);
    MessageHolder onNewMessageHolder(Message message);
    void onClick(Activity activity, View rootView, Message message);
    void onLongClick(Activity activity, View rootView, Message message);

}
