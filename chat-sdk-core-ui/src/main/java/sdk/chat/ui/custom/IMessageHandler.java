package sdk.chat.ui.custom;

import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;

/**
 * Use this class to customize messages and their click behaviour
 */

public interface IMessageHandler {

    List<Byte> getTypes();
    boolean hasContentFor(MessageHolder holder);

    void onBindMessageHolders(Context context, MessageHolders holders);
    MessageHolder onNewMessageHolder(Message message);
    boolean onClick(ChatActivity activity, View rootView, Message message);
    boolean onLongClick(ChatActivity activity, View rootView, Message message);

}
