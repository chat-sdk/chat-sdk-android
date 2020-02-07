package co.chatsdk.ui.chat.model;

import co.chatsdk.core.dao.Message;

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

}
