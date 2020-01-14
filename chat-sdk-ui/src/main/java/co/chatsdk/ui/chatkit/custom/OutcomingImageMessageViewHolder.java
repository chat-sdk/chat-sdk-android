package co.chatsdk.ui.chatkit.custom;

import android.util.Pair;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.ui.chatkit.model.MessageHolder;

/*
 * Created by troy379 on 05.04.17.
 */
public class OutcomingImageMessageViewHolder
        extends MessageHolders.OutcomingImageMessageViewHolder<MessageHolder> {

    public OutcomingImageMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(MessageHolder message) {
        super.onBind(message);

        time.setText(message.getStatus() + " " + time.getText());
    }

    //Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
    @Override
    protected Object getPayloadForImageLoader(MessageHolder message) {
        //For example you can pass size of placeholder before loading
        return new Pair<>(100, 100);
    }
}