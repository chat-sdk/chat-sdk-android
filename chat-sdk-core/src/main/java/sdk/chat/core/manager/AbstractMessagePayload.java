package sdk.chat.core.manager;

import android.graphics.drawable.Drawable;

import sdk.chat.core.dao.Message;

public abstract class AbstractMessagePayload implements MessagePayload {

    protected Message message;
    protected MessagePayload reply;

    public AbstractMessagePayload(Message message) {
        this(message, null);
    }

    public AbstractMessagePayload(Message message, MessagePayload reply) {
        this.message = message;
        this.reply = reply;
    }

    @Override
    public String lastMessageText() {
        return getText();
    }

    @Override
    public String getText() {
        if (reply != null) {
            return message.getReply();
        }
        return message.getText();
    }

    @Override
    public MessagePayload replyPayload() {
        return reply;
    }

    @Override
    public Drawable getPlaceholder() {
        return null;
    }
}