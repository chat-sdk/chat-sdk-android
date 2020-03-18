package co.chatsdk.ui.chat.model;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.dao.Message;

public class SystemMessageHolder extends MessageHolder implements MessageContentType {
    public SystemMessageHolder(Message message) {
        super(message);
    }
}
