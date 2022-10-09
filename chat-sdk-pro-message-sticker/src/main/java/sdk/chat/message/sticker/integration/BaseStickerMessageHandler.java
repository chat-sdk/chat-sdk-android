package sdk.chat.message.sticker.integration;

import io.reactivex.Completable;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.StickerMessageHandler;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.message.sticker.StickerMessagePayload;
import sdk.chat.message.sticker.keyboard.StickerKeyboardOverlayFragment;

/**
 * Created by ben on 10/11/17.
 */

public class BaseStickerMessageHandler extends AbstractMessageHandler implements StickerMessageHandler {

    @Override
    public Completable sendMessageWithSticker(final String name, final Thread thread) {
        return sendMessageWithSticker(name, null, thread);
    }

    @Override
    public Completable sendMessageWithSticker(final String name, final String url, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Sticker), thread, message -> {
            message.setText(name);
            message.setValueForKey(name, Keys.MessageStickerName);
            if (!StringChecker.isNullOrEmpty(url)) {
                message.setImageURL(url);
            }
        }).run();
    }
    @Override
    public AbstractKeyboardOverlayFragment keyboardOverlay(KeyboardOverlayHandler sender) {
        AbstractKeyboardOverlayFragment fragment = ChatSDK.feather().instance(StickerKeyboardOverlayFragment.class);
        fragment.setHandler(sender);
        return fragment;
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
