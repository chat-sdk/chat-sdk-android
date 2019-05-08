package co.chatsdk.core.handlers;

import java.io.File;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Pepe Becker on 01/05/2018.
 */

public interface FileMessageHandler extends MessageHandler {
    Completable sendMessageWithFile(String name, String mimeType, File file, final Thread thread);
}
