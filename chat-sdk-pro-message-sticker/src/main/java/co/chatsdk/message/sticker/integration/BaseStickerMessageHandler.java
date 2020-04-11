package co.chatsdk.message.sticker.integration;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.StickerMessageHandler;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.types.MessageType;
import io.reactivex.Completable;

/**
 * Created by ben on 10/11/17.
 */

public class BaseStickerMessageHandler implements StickerMessageHandler {
    @Override
    public Completable sendMessageWithSticker(final String stickerImageName, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Sticker), thread, message -> {
            message.setText(stickerImageName);
            message.setValueForKey(stickerImageName, Keys.MessageStickerName);
        }).run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageStickerName);
    }
}
