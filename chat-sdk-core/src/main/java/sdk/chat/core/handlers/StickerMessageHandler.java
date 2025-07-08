package sdk.chat.core.handlers;

import io.reactivex.Completable;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface StickerMessageHandler extends MessageHandler {
    Completable sendMessageWithSticker(String stickerImageName, final ThreadX thread);
    Completable sendMessageWithSticker(final String name, final String url, final ThreadX thread);
    AbstractKeyboardOverlayFragment keyboardOverlay(KeyboardOverlayHandler sender);
}
