package sdk.chat.ui.chat.model;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.ReadStatus;

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
