package co.chatsdk.core.interfaces;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.recyclerview.widget.RecyclerView;
import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.MessageType;

/**
 * Created by ben on 10/11/17.
 */

public interface MessageDisplayHandler {

    void updateMessageCellView (Message message, AbstractMessageViewHolder viewHolder, Context context);
    String displayName (Message message);
    AbstractMessageViewHolder newViewHolder(boolean isReply, Activity activity);

}
