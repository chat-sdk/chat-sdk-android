package sdk.chat.message.sticker.provider;

import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.message.sticker.keyboard.KeyboardOverlayStickerFragment;

public class StickerModuleProvider {

    public AbstractKeyboardOverlayFragment keyboardOverlay(KeyboardOverlayHandler sender) {
        AbstractKeyboardOverlayFragment fragment = new KeyboardOverlayStickerFragment();
        fragment.setHandler(sender);
        return fragment;
    }

}
