package sdk.chat.ui.chat.model;

import com.stfalcon.chatkit.commons.models.IMessage;

import org.pmw.tinylog.Logger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendProgress;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.ui.binders.MessageBinder;
import sdk.chat.ui.module.UIModule;

public class MessageHolder implements IMessage {

    public Message message;
    protected UserHolder userHolder = null;
    protected MessageSendProgress progress = null;

    protected boolean isGroup;
    protected boolean previousSenderEqualsSender;
    protected boolean showDate;
    protected String quotedImageURL;

    public MessageHolder(Message message) {
        this.message = message;
        update();
    }

    public void update() {
        Message nextMessage = message.getNextMessage();
        Message previousMessage = message.getPreviousMessage();

        previousSenderEqualsSender = previousMessage != null && message.getSender().equalsEntity(previousMessage.getSender());

        DateFormat format = UIModule.shared().getMessageBinder().messageTimeComparisonDateFormat(ChatSDK.ctx());
        showDate = nextMessage == null || !format.format(message.getDate()).equals(format.format(nextMessage.getDate()));
        isGroup = message.getThread().typeIs(ThreadType.Group);

        if (message.isReply()) {
            quotedImageURL = ChatSDK.getMessageImageURL(message);
        }
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
        if (message == null || message.getDate() == null) {
            Logger.debug("HERE");
        }
        return message.getDate();
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
        if (isReply()) {
            return quotedImageURL;
        }
        return null;
    }

    public boolean showNames() {
        return UIModule.config().showNamesInGroupChatView && isGroup && !previousSenderEqualsSender;
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
