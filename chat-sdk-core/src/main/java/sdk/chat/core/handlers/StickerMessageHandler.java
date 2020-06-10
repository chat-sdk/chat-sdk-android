package sdk.chat.core.handlers;

import sdk.chat.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface StickerMessageHandler extends MessageHandler {
    Completable sendMessageWithSticker(String stickerImageName, final Thread thread);
}
