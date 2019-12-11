package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.MessageDisplayHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface MessageHandler {

    /**
     * How should this text be represented as a text string
     * @param message
     * @return - A string representation of the text
     */
    String textRepresentation (Message message);

}
