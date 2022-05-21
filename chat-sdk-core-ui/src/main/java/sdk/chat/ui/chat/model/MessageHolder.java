package sdk.chat.ui.chat.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Message;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.Progress;
import sdk.chat.core.types.ReadStatus;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.view_holders.v2.MessageDirection;
import sdk.guru.common.DisposableMap;

public class MessageHolder implements IMessage, Consumer<Throwable> {

    public Message message;
    protected MessagePayload payload;

    protected Message nextMessage;
    protected Message previousMessage;

    protected UserHolder userHolder;

    protected boolean isGroup;
    protected boolean previousSenderEqualsSender;
    protected boolean nextSenderEqualsSender;
    protected boolean showDate;
    protected String quotedImageURL;
    protected Drawable quotedImagePlaceholder;
    protected MessageDirection direction;

    protected String typingText = null;
    protected DisposableMap dm = new DisposableMap();

    protected ReadStatus readStatus = null;

//    protected Date date;
    protected MessageSendStatus sendStatus = null;
    protected float uploadPercentage = -1;
    protected float fileSize = -1;
    protected boolean isReply;

    protected boolean isDirty = true;

    public MessageHolder(Message message) {
        this.message = message;
        this.payload = ChatSDK.getMessagePayload(message);

        dm.add(ChatSDK.events().prioritySourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageProgressUpdated))
                .filter(NetworkEvent.filterMessageEntityID(getId()))
                .subscribe(networkEvent -> {
                    Progress progress = networkEvent.getProgress();
                    updateProgress(progress);
                }, this));

        dm.add(ChatSDK.events().prioritySourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageSendStatusUpdated))
                .filter(NetworkEvent.filterMessageEntityID(getId()))
                .subscribe(networkEvent -> {
                    MessageSendStatus status = networkEvent.getMessageSendStatus();
                    updateSendStatus(status);
                }, this));

        dm.add(ChatSDK.events().prioritySourceOnSingle()
                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated))
                .filter(NetworkEvent.filterMessageEntityID(getId()))
                .subscribe(networkEvent -> {
                    updateReadStatus();
                }, this));

        userHolder = ChatSDKUI.provider().holderProvider().getUserHolder(message.getSender());

        updateSendStatus(message.getMessageStatus());
        updateProgress(null);
        updateNextAndPreviousMessages();
        updateReadStatus();

        MessagePayload replyPayload = payload.replyPayload();
        if (replyPayload instanceof ImageMessagePayload) {
            ImageMessagePayload ip = (ImageMessagePayload) replyPayload;
            quotedImageURL = ip.imageURL();
            quotedImagePlaceholder = ip.getPlaceholder();
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
        showDate = nextMessage == null || !(format.format(message.getDate()).equals(format.format(nextMessage.getDate())) && nextSenderEqualsSender);
        isGroup = message.getThread().typeIs(ThreadType.Group);
    }

    public void updateSendStatus(MessageSendStatus status) {
        isDirty = isDirty || status != sendStatus;
        sendStatus = status;
    }

    public void updateProgress(Progress progress) {
        if (progress == null) {

            isDirty = isDirty || this.uploadPercentage >= 0;
            isDirty = isDirty || this.fileSize >= 0;

            uploadPercentage = -1;
            fileSize = -1;

        } else {

            float uploadPercentage = Math.round(progress.asFraction() * 100);
            isDirty = isDirty || uploadPercentage == this.uploadPercentage;
            this.uploadPercentage = uploadPercentage;

            float fileSize = (float) Math.floor(progress.getTotalBytes() / 1000f);
            isDirty = isDirty || fileSize == this.fileSize;
            this.fileSize = fileSize;

        }
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
//        if (typingText != null) {
//            return typingText;
//        } else {
            return payload.getText();
//        }
    }

    @Override
    public String getPreview() {
        if (typingText != null) {
            return typingText;
        } else {
            return payload.lastMessageText();
        }
    }

    @Override
    public UserHolder getUser() {
        return userHolder;
    }

    @Override
    public Date getCreatedAt() {
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
        return sendStatus;
    }

    public float getUploadPercentage() {
        return uploadPercentage;
    }

    public float getFileSize() {
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
        if (payload.replyPayload() != null) {
            return payload.replyPayload().getText();
        }
        return null;
//        return message.getText();
    }

    public String getQuotedImageUrl() {
        return quotedImageURL;
    }

    public Drawable getQuotedPlaceholder() {
        return quotedImagePlaceholder;
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

    @Override
    public void accept(Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }

    public @DrawableRes int defaultPlaceholder() {
        return R.drawable.icn_100_profile;
    }
}
