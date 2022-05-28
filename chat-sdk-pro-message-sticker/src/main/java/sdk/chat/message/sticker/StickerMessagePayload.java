package sdk.chat.message.sticker;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.ImageMessageUtil;
import sdk.chat.core.utils.Size;
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
            String stickerName = (String) message.valueForKey(Keys.MessageStickerName);
            return provider.imageURL(stickerName);
        }
        return null;
    }

    @Override
    public Size getSize() {
        return new Size(ImageMessageUtil.pxToDp(StickerMessageModule.config().maxSize));
    }


    @Override
    public String lastMessageText() {
        return ChatSDK.getString(R.string.sticker_message);
    }}
