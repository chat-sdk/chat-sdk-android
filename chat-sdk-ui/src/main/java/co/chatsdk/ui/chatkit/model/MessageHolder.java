package co.chatsdk.ui.chatkit.model;

import android.view.View;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;

public class MessageHolder implements IMessage, MessageContentType.Image {

    public Message message;
    protected ReadStatus readStatus = null;
    protected UserHolder userHolder = null;

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
        if (userHolder == null) {
            userHolder = new UserHolder(message.getSender());
        }
        return userHolder;
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

    public Integer getReadStatusResourceId() {
        ReadStatus status = message.getReadStatus();

        // Hide the read receipt for public threads
        if(!message.getThread().typeIs(ThreadType.Public) && ChatSDK.readReceipts() != null || status.is(ReadStatus.hide())) {
            return null;
        }

        int resource = R.drawable.ic_message_received;

        if(status.is(ReadStatus.delivered())) {
            resource = R.drawable.ic_message_delivered;
        }
        if(status.is(ReadStatus.read())) {
            resource = R.drawable.ic_message_read;
        }

        return resource;
    }
}
