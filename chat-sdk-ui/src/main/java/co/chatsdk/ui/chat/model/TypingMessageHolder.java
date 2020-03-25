package co.chatsdk.ui.chat.model;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.ReadStatus;

public class TypingMessageHolder extends MessageHolder {

    protected String text;

    public TypingMessageHolder(Message message, String text) {
        super(message);
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    public ReadStatus getReadStatus() {
        return ReadStatus.none();
    }


}
