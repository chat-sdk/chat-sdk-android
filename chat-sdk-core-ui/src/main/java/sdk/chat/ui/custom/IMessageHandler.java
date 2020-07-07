package sdk.chat.ui.custom;

import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;

/**
 * Use this class to customize messages and their click behaviour
 */

public interface IMessageHandler extends MessageHolders.ContentChecker<MessageHolder> {

    void onBindMessageHolders(Context context, MessageHolders holders);
    MessageHolder onNewMessageHolder(Message message);
    void onClick(ChatActivity activity, View rootView, Message message);
    void onLongClick(ChatActivity activity, View rootView, Message message);

}
