package co.chatsdk.ui.chat.model;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.ui.binders.MessageBinder;

public class MessageHolder implements IMessage {

    public Message message;
    protected UserHolder userHolder = null;
    protected MessageSendProgress progress = null;

    protected boolean isGroup;
    protected boolean previousSenderEqualsSender;
    protected boolean showDate;

    public MessageHolder(Message message) {
        this.message = message;
        update();
    }

    public void update() {
        Message nextMessage = message.getNextMessage();
        Message previousMessage = message.getPreviousMessage();

        previousSenderEqualsSender = previousMessage == null || !message.getSender().equalsEntity(previousMessage.getSender());

        DateFormat format = MessageBinder.messageTimeComparisonDateFormat(ChatSDK.shared().context());
        showDate = nextMessage == null || !format.format(message.getDate().toDate()).equals(format.format(nextMessage.getDate().toDate()));
        isGroup = message.getThread().typeIs(ThreadType.Group);
    }


    @Override
    public String getId() {
        return message.getEntityID();
    }

    @Override
    public String getText() {
        if (isReply()) {
            return message.getReply();
        }
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

    public MessageSendStatus getSendStatus() {
        return message.getMessageStatus();
    }

    public boolean isReply() {
        return message.isReply();
    }

    public String getQuotedText() {
        return message.getText();
    }

    public String getQuotedImageUrl() {
        return message.imageURL();
    }

    public boolean showNames() {
        return isGroup && previousSenderEqualsSender;
    }

    public boolean showDate() {
        return showDate;
    }

    public String getIcon() {
        return null;
//        return "http://flathash.com/ben";
    }

    public static List<Message> toMessages(List<MessageHolder> messageHolders) {
        ArrayList<Message> messages = new ArrayList<>();
        for (MessageHolder mh: messageHolders) {
            messages.add(mh.getMessage());
        }
        return messages;
    }

}
