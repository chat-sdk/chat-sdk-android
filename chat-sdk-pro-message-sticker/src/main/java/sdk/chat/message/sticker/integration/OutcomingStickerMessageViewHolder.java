package sdk.chat.message.sticker.integration;

import android.view.View;

import androidx.annotation.NonNull;

import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.view_holders.v2.MessageDirection;
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder;

public class OutcomingStickerMessageViewHolder extends V2ImageMessageViewHolder<StickerMessageHolder> {
    public OutcomingStickerMessageViewHolder(View itemView) {
        super(itemView, MessageDirection.Outcoming);
    }

    @Override
    public void loadImage(@NonNull StickerMessageHolder holder) {
        ChatSDKUI.provider().imageLoader().load(
                getImage(),
                holder.getImageUrl(),
                holder.placeholder(),
                holder.getSize(),
                holder.getText().contains(".gif")
        );
    }

}
