package sdk.chat.ui.interfaces;

import java.io.File;

import sdk.chat.core.ui.KeyboardOverlayHandler;

/**
 * Created by ben on 10/11/17.
 */

public interface TextInputDelegate extends KeyboardOverlayHandler {

    void sendAudio (final File file, String mimeType, long duration);
    void sendMessage(String text);
    boolean keyboardOverlayAvailable();

}
