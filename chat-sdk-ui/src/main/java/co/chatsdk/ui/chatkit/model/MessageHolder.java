package co.chatsdk.ui.chatkit.model;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.R;

public class MessageHolder implements IMessage {

    public Message message;
    protected ReadStatus readStatus = null;
    protected UserHolder userHolder = null;
    protected MessageSendProgress progress = null;

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
        if (progress == null) {
            return message.getMessageStatus();
        }
        return progress.status;
    }

    public Integer getUploadPercentage() {
        if (progress != null && progress.uploadProgress != null) {
            return Math.round(progress.uploadProgress.asFraction() * 100);
        }
        return null;
    }

    public void setProgress(MessageSendProgress progress) {
        this.progress = progress;
    }

    public ReadStatus getReadStatus() {
        return message.getReadStatus();
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

    public static List<MessageHolder> fromMessages(List<Message> messages) {
        ArrayList<MessageHolder> messageHolders = new ArrayList<>();
        for (Message m: messages) {
            messageHolders.add(fromMessage(m));
        }
        return messageHolders;
    }

    public static MessageHolder fromMessage(Message message) {
        if (message.getMessageType().is(MessageType.Image, MessageType.Location)) {
            return new ImageMessageHolder(message);
        }
        return new MessageHolder(message);
    }

    public static List<Message> toMessages(List<MessageHolder> messageHolders) {
        ArrayList<Message> messages = new ArrayList<>();
        for (MessageHolder mh: messageHolders) {
            messages.add(mh.getMessage());
        }
        return messages;
    }

}
