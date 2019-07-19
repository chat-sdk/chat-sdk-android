package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.MessageDisplayHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface MessageHandler {

    /**
     * How should this message be represented as a text string
     * @param message
     * @return - A string representation of the message
     */
    String textRepresentation (Message message);

}
