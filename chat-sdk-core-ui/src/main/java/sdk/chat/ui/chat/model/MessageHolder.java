package sdk.chat.ui.chat.model;

import android.content.Context;

import com.stfalcon.chatkit.commons.models.IMessage;

import org.pmw.tinylog.Logger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import sdk.chat.core.dao.Message;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendProgress;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.view_holders.v2.MessageDirection;
import sdk.guru.common.DisposableMap;

public class MessageHolder implements IMessage {

    public Message message;
    protected Message nextMessage;
    protected Message previousMessage;

    protected UserHolder userHolder;

    protected boolean isGroup;
    protected boolean previousSenderEqualsSender;
    protected boolean nextSenderEqualsSender;
    protected boolean showDate;
    protected String quotedImageURL;
    protected MessageDirection direction;

    protected String typingText = null;
    protected DisposableMap dm = new DisposableMap();

    protected ReadStatus readStatus = null;

    protected Date date;
    protected MessageSendStatus sendStatus = null;
    protected Integer uploadPercentage;
    protected Double fileSize;
    protected boolean isReply;

    protected boolean isDirty = true;

    public MessageHolder(Message message) {
        this.message = message;

        dm.add(ChatSDK.events().prioritySourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusUpdated))
                .filter(NetworkEvent.filterMessageEntityID(getId()))
                .subscribe(networkEvent -> {
                    MessageSendProgress progress = networkEvent.getMessageSendProgress();
                    updateSendStatus(progress);
                }));

        dm.add(ChatSDK.events().prioritySourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated))
//                .filter(NetworkEvent.filterMessageEntityID(getId()))
                .subscribe(networkEvent -> {
                    if (networkEvent.getMessage().getEntityID().equals(getId())) {
                        Logger.debug("MessageHolder: " + networkEvent.debugText());

                        updateReadStatus();

                    }
                }));

//        dm.add(ChatSDK.events().sourceOnMain()
//                .filter(NetworkEvent.filterType(EventType.MessageUpdated, EventType.MessageAdded, EventType.MessageRemoved))
//                .filter(NetworkEvent.filterMessageEntityID(getId()))
//                .subscribe(networkEvent -> {
//                    updateNextAndPreviousMessages();
//                }));

        userHolder = ChatSDKUI.provider().holderProvider().getUserHolder(message.getSender());
        date = message.getDate();

        updateSendStatus(null);
        updateNextAndPreviousMessages();
        updateReadStatus();

        if (message.isReply()) {
            quotedImageURL = ChatSDK.getMessageImageURL(message);
        }

        isReply = message.isReply();
        direction = getUser().isMe() ? MessageDirection.Outcoming : MessageDirection.Incoming;

    }

    public void updateNextAndPreviousMessages() {
        Message nextMessage = message.getNextMessage();
        Message previousMessage = message.getPreviousMessage();

        if (!isDirty) {
            String oldNextMessageId = this.nextMessage != null ? this.nextMessage.getEntityID() : "";
            String newNextMessageId = nextMessage != null ? nextMessage.getEntityID() : "";
            isDirty = !oldNextMessageId.equals(newNextMessageId);
        }

        if (!isDirty) {
            String oldPreviousMessageId = this.previousMessage != null ? this.previousMessage.getEntityID() : "";
            String newPreviousMessageId = previousMessage != null ? previousMessage.getEntityID() : "";
            isDirty = !oldPreviousMessageId.equals(newPreviousMessageId);
        }

        this.nextMessage = nextMessage;
        this.previousMessage = previousMessage;

        previousSenderEqualsSender = previousMessage != null && message.getSender().equalsEntity(previousMessage.getSender());
        nextSenderEqualsSender = nextMessage != null && message.getSender().equalsEntity(nextMessage.getSender());

        DateFormat format = UIModule.shared().getMessageBinder().messageTimeComparisonDateFormat(ChatSDK.ctx());
        showDate = nextMessage == null || !format.format(message.getDate()).equals(format.format(nextMessage.getDate())) && nextSenderEqualsSender;
        isGroup = message.getThread().typeIs(ThreadType.Group);
    }

    public void updateSendStatus(MessageSendProgress progress) {
        MessageSendStatus status;

        if (progress == null) {
            status = message.getMessageStatus();

            isDirty = isDirty || this.uploadPercentage != null;
            isDirty = isDirty || this.fileSize != null;

            uploadPercentage = null;
            fileSize = null;

        } else {
            status = progress.status;

            Integer uploadPercentage = Math.round(progress.uploadProgress.asFraction() * 100);
            isDirty = isDirty || uploadPercentage.equals(this.uploadPercentage);
            this.uploadPercentage = uploadPercentage;

            Double fileSize = Math.floor(progress.uploadProgress.getTotalBytes() / 1000);
            isDirty = isDirty || fileSize.equals(this.fileSize);
            this.fileSize = fileSize;

        }
        isDirty = isDirty || status == sendStatus;
        sendStatus = status;
    }

    public void updateReadStatus() {
        ReadStatus status = message.getReadStatus();
        isDirty = isDirty || readStatus != status;
        readStatus = status;
    }

    @Override
    public String getId() {
        return message.getEntityID();
    }

    @Override
    public String getText() {
        if (typingText != null) {
            return typingText;
        }
        if (isReply()) {
            return message.getReply();
        }
        return message.getText();
    }

    @Override
    public UserHolder getUser() {
        return userHolder;
    }

    @Override
    public Date getCreatedAt() {
        return date;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MessageHolder && getId().equals(((MessageHolder)object).getId());
    }

    public Message getMessage() {
        return message;
    }

    public MessageSendStatus getStatus() {
        return sendStatus;
    }

    public Integer getUploadPercentage() {
        return uploadPercentage;
    }

    public Double getFileSize() {
        return fileSize;
    }

//    public void setProgress(MessageSendProgress progress) {
//        this.progress = progress;
//    }

    public ReadStatus getReadStatus() {
        return readStatus;
    }

    public MessageSendStatus getSendStatus() {
        return sendStatus;
    }

    public boolean isReply() {
        return isReply;
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
    }

    public static List<Message> toMessages(List<MessageHolder> messageHolders) {
        ArrayList<Message> messages = new ArrayList<>();
        for (MessageHolder mh: messageHolders) {
            messages.add(mh.getMessage());
        }
        return messages;
    }

    public boolean canResend() {
        return message.canResend();
    }

    public MessageDirection direction() {
        return direction;
    }

    public Single<String> save(final Context context) {
        return Single.just("");
    }

    public boolean canSave() {
        return false;
    }

    public void setTypingText(String text) {
        typingText = text;
    }

    public boolean isTyping() {
        return typingText != null;
    }

    public boolean isDirty() {
        return isDirty || userHolder.isDirty();
    }

    public void makeClean() {
        isDirty = false;
        userHolder.makeClean();
    }

    public Message previousMessage() {
        return previousMessage;
    }

    public Message nextMessage() {
        return nextMessage;
    }

}
