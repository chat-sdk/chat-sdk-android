package co.chatsdk.ui.chatkit.model;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;

public class MessageHolder implements IMessage, MessageContentType.Image {

    public Message message;

    public MessageHolder(Message message) {
        this.message = message;
    }

    @Override
    public String getId() {
        return message.getEntityID();
    }

    @Override
    public String getText() {
        return message.getText();
    }

    @Override
    public UserHolder getUser() {
        return new UserHolder(message.getSender());
    }

    @Override
    public Date getCreatedAt() {
        return message.getDate().toDate();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MessageHolder && getId().equals(((MessageHolder)object).getId());
    }

    public Message getMessage() {
        return message;
    }

    public MessageSendStatus getStatus() {
        return message.getMessageStatus();
    }

    public ReadStatus getReadStatus() {
        return message.getReadStatus();
    }

    @Nullable
    @Override
    public String getImageUrl() {
        if (message.getMessageType().is(MessageType.Image)) {
            return message.stringForKey(Keys.MessageImageURL);
        }
        return null;
    }
}
