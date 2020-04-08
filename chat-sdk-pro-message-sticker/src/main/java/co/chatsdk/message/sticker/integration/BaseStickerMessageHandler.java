package co.chatsdk.message.sticker.integration;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.StickerMessageHandler;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.types.MessageType;
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
