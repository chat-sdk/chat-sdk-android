package co.chatsdk.message.sticker.integration;

import android.view.View;

import co.chatsdk.message.sticker.module.StickerMessageModule;
import co.chatsdk.ui.utils.ImageLoaderPayload;
import co.chatsdk.ui.view_holders.base.BaseIncomingImageMessageViewHolder;

public class IncomingStickerMessageViewHolder extends BaseIncomingImageMessageViewHolder<StickerMessageHolder> {
    public IncomingStickerMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    protected Object getPayloadForImageLoader(StickerMessageHolder message) {
        return new ImageLoaderPayload(StickerMessageModule.config().maxSize, StickerMessageModule.config().maxSize, co.chatsdk.ui.R.drawable.icn_200_image_message_placeholder, 0);
    }

}
