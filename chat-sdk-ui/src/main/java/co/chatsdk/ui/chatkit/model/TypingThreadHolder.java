package co.chatsdk.ui.chatkit.model;

import co.chatsdk.core.dao.Thread;

public class TypingThreadHolder extends ThreadHolder {

    protected String text;

    public TypingThreadHolder(Thread thread, String text) {
        super(thread);
        this.text = text;
    }

    @Override
    public MessageHolder getLastMessage() {
        if (text != null) {
            return new TypingMessageHolder(thread.lastMessage(), text);
        }
        return super.getLastMessage();
    }

}
