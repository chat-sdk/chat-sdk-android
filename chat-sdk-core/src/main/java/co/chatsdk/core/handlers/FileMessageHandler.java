package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Observable;

/**
 * Created by Pepe Becker on 01/05/2018.
 */

public interface FileMessageHandler extends MessageDisplayHandler {
    Observable<MessageSendProgress> sendMessageWithFile(String name, String mimeType, byte[] data, final Thread thread);
}
