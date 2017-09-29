package co.chatsdk.core.handlers;

import android.content.Context;
import android.widget.RelativeLayout;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import co.chatsdk.core.dao.Thread;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AudioMessageHandler {

    /**
     * @brief Send an audio message
     */
    Observable<MessageSendProgress> sendMessage (final Recording recording, final Thread thread);

    void updateMessageCellView (Message message, RelativeLayout layout, Context context);

}
