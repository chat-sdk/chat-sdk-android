package co.chatsdk.ui.chatkit.view_holders;

import android.util.Pair;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatusFormatter;
import co.chatsdk.ui.chatkit.model.ImageMessageHolder;
import co.chatsdk.ui.chatkit.model.MessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class OutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<ImageMessageHolder> {

    public OutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(ImageMessageHolder message) {
        super.onBind(message);

        String status = MessageSendStatusFormatter.format(ChatSDK.shared().context(), message.getStatus(), message.getUploadPercentage());
        String timeString = status + " " + time.getText();

        time.setText(timeString);
    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        return message;
    }
}