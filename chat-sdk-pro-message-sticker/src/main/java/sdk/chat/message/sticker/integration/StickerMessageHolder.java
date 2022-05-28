package sdk.chat.message.sticker.integration;

import sdk.chat.core.dao.Message;
import sdk.chat.message.sticker.StickerMessagePayload;
import sdk.chat.ui.chat.model.ImageMessageHolder;


public class StickerMessageHolder extends ImageMessageHolder {
    public StickerMessageHolder(Message message) {
        super(message);
    }

    public StickerMessagePayload getPayload() {
        if (payload instanceof StickerMessagePayload) {
            return (StickerMessagePayload) payload;
        }
        return null;
    }

}
