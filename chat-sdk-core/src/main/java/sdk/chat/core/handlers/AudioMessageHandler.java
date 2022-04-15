package sdk.chat.core.handlers;

import android.content.Context;

import java.io.File;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface AudioMessageHandler extends MessageHandler {

    /**
     * Send an audio text
     */
    Completable sendMessage(Context context, final File file, String mimeType, long duration, final Thread thread);
    void setCompressionEnabled(boolean enabled);
    AbstractKeyboardOverlayFragment getOverlay(KeyboardOverlayHandler handler);

}
