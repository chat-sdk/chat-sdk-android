package sdk.chat.core.handlers;

import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.types.MessageType;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface MessageHandler {
    MessagePayload payloadFor(Message message);
    boolean isFor(MessageType type);
}
