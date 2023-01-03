package sdk.chat.core.manager;

import android.graphics.drawable.Drawable;

public interface MessagePayload {

    /**
     * The text of the message (if it exists)
     * @return
     */
    String getText();

    /**
     * The image associated with the message
     * @return
     */
    String imageURL();

    /**
     * The placeholder drawable - available immediately
     * @return
     */
    Drawable getPlaceholder();

    /**
     * Text to show on thread screen - i.e. a text summary. For text messages
     * this will be the text, for non-text message types this will be a text
     * description of the message type i.e. File Message
     * @return
     */
    String lastMessageText();

    /**
     * The message payload of the original message if this is a reply
     * @return
     */
    MessagePayload replyPayload();

}
