package co.chatsdk.ui.chat.model;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Message;

public class SystemMessageHolder extends MessageHolder implements MessageContentType {
    public SystemMessageHolder(Message message) {
        super(message);
    }
}
