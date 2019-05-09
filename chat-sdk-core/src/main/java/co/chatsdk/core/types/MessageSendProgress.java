package co.chatsdk.core.types;

import co.chatsdk.core.dao.Message;

/**
 * Created by ben on 9/29/17.
 */

public class MessageSendProgress {

    public Message message;
    public Progress uploadProgress;
    public MessageSendStatus status;

    public MessageSendProgress (Message message) {
        // Copy the message status here because we want it to be tied to the progress event
        // If we use the message value while multi-threading, we can get a situation where
        // the status updates too rapidly for the main thread and the notifications all come
        // at once. So rather than status: 1, 2, 3. We get status: 3, 3, 3
        this(message, message.getMessageStatus(),null);
    }

    public MessageSendProgress (Message message, MessageSendStatus status, Progress progress) {
        this.message = message;
        this.uploadProgress = progress;
        this.status = status;
    }

    public MessageSendStatus getStatus () {
        return status;
    }

}
