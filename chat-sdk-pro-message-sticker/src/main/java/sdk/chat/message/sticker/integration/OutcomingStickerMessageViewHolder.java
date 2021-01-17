package sdk.chat.message.sticker.integration;

import android.view.View;

import sdk.chat.message.sticker.R;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.ui.utils.ImageLoaderPayload;
import sdk.chat.ui.view_holders.base.BaseOutcomingImageMessageViewHolder;

public class OutcomingStickerMessageViewHolder extends BaseOutcomingImageMessageViewHolder<StickerMessageHolder> {
    public OutcomingStickerMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }

    @Override
    protected Object getPayloadForImageLoader(StickerMessageHolder message) {
        return new ImageLoaderPayload(StickerMessageModule.config().maxSize,
                StickerMessageModule.config().maxSize,
                R.drawable.icn_200_image_message_placeholder,
                0,
                message.getText().contains(".gif"));
    }

}
