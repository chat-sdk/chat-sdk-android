package sdk.chat.message.sticker.integration;

import io.reactivex.Completable;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.StickerMessageHandler;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.message.sticker.StickerMessagePayload;
import sdk.chat.message.sticker.keyboard.KeyboardOverlayStickerFragment;

/**
 * Created by ben on 10/11/17.
 */

public class BaseStickerMessageHandler extends AbstractMessageHandler implements StickerMessageHandler {
    @Override
    public Completable sendMessageWithSticker(final String stickerImageName, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Sticker), thread, message -> {
            message.setText(stickerImageName);
            message.setValueForKey(stickerImageName, Keys.MessageStickerName);
        }).run();
    }

    @Override
    public AbstractKeyboardOverlayFragment keyboardOverlay(KeyboardOverlayHandler sender) {
        return new KeyboardOverlayStickerFragment(sender);
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        return new StickerMessagePayload(message);
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Sticker);
    }

}
