package sdk.chat.ui.chat.model;

import sdk.chat.core.dao.ThreadX;

public class TypingThreadHolder extends ThreadHolder {

    protected String text;

    public TypingThreadHolder(ThreadX thread, String text) {
        super(thread);
        this.text = text;
    }

    @Override
    public MessageHolder getLastMessage() {
        if (text != null && thread.lastMessage() != null) {
            return new TypingMessageHolder(thread.lastMessage(), text);
        }
        return super.getLastMessage();
    }

    @Override
    public int getUnreadCount() {
        return 0;
    }

}
