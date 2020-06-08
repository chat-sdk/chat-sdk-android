package sdk.chat.message.sticker.integration;

import androidx.annotation.Nullable;

import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.model.ImageMessageHolder;


public class StickerMessageHolder extends ImageMessageHolder {
    public StickerMessageHolder(Message message) {
        super(message);
    }

    @Nullable
    public String getImageUrl() {
        return ChatSDK.stickerMessage().getImageURL(message);
    }

}
