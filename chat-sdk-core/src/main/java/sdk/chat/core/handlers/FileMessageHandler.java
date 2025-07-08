package sdk.chat.core.handlers;

import java.io.File;

import sdk.chat.core.dao.ThreadX;
import io.reactivex.Completable;

/**
 * Created by Pepe Becker on 01/05/2018.
 */

public interface FileMessageHandler extends MessageHandler {
    Completable sendMessageWithFile(String name, String mimeType, File file, final ThreadX thread);
}
