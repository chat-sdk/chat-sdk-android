package sdk.chat.custom_message.sticker;

import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import org.pmw.tinylog.Logger;

public class StickerViewHolder extends MessageHolders.BaseMessageViewHolder<StickerMessageHolder> {

    public StickerViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    public void onBind(StickerMessageHolder stickerMessage) {
        Logger.debug("");
    }
}
