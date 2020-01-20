package co.chatsdk.ui.chatkit.custom;

import android.util.Pair;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

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

        time.setText(message.getStatus() + " " + time.getText());
    }

    @Override
    protected Object getPayloadForImageLoader(ImageMessageHolder message) {
        return message;
    }
}