package sdk.chat.custom_message.sticker;

import androidx.annotation.Nullable;

import co.chatsdk.core.dao.Message;
import co.chatsdk.ui.chat.model.ImageMessageHolder;

public class StickerMessageHolder extends ImageMessageHolder {

    public StickerMessageHolder(Message message) {
        super(message);
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return null;
    }
}
