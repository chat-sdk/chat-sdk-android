package sdk.chat.message.sticker;

import static sdk.chat.core.utils.Device.dpToPx;

import android.graphics.drawable.Drawable;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Size;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.message.sticker.module.StickerMessageModule;
import sdk.chat.message.sticker.provider.StickerPackProvider;

public class StickerMessagePayload extends ImageMessagePayload {

    public StickerPackProvider provider;

    public StickerMessagePayload(Message message) {
        super(message);
        this.provider = ChatSDK.feather().instance(StickerPackProvider.class);
    }

    @Override
    public String getText() {
        String name = message.stringForKey(Keys.MessageStickerName);
        String [] parts = name.split(".");
        if (parts.length == 2) {
            return parts[0];
        }
        return name;
    }

    @Override
    public String imageURL() {
        if (message.getMessageType().is(MessageType.Sticker)  || message.getReplyType().is(MessageType.Sticker)) {
            if (!StringChecker.isNullOrEmpty(message.getImageURL())) {
                return message.getImageURL();
            } else {
                String stickerName = (String) message.valueForKey(Keys.MessageStickerName);
                return provider.imageURL(stickerName);
            }
        }
        return null;
    }

    @Override
    public Drawable getPlaceholder() {
        return null;
    }

    @Override
    public Size getSize() {
        return new Size(dpToPx(StickerMessageModule.config().maxSize));
    }


    @Override
    public String lastMessageText() {
        return ChatSDK.getString(sdk.chat.core.R.string.sticker_message);
    }}
