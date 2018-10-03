package co.chatsdk.core.handlers;

import java.util.Map;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.CustomMessageHandler;
import co.chatsdk.core.types.MessageSendProgress;
import io.reactivex.Observable;

/**
 * Created by Pepe Becker on 01/05/2018.
 */

public interface FileMessageHandler extends CustomMessageHandler {

    Observable<MessageSendProgress> sendMessageWithFile(Map<String, Object> file, final Thread thread);

}
