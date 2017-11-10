package co.chatsdk.core.interfaces;

import android.content.Context;

import co.chatsdk.core.dao.Message;

/**
 * Created by ben on 10/11/17.
 */

public interface CustomMessageHandler {

    void updateMessageCellView (Message message, Object viewHolder, Context context);

}
