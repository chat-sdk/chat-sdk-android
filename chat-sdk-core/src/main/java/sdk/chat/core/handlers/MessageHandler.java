package sdk.chat.core.handlers;

import sdk.chat.core.dao.Message;

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

    String getImageURL(Message message);

    /**
     * Human readable message type
     * @param message
     * @return
     */
    String toString(Message message);

}
