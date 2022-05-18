package sdk.chat.core.manager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
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
    public String previewText() {
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
    public List<String> remoteURLs() {
        return new ArrayList<>();
    }

    @Override
    public Completable downloadMessageContent() {
        return Completable.complete();
    }

    @Override
    public MessagePayload replyPayload() {
        return reply;
    }

}
