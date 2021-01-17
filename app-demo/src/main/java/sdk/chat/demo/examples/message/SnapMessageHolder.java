package sdk.chat.demo.examples.message;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.chat.model.MessageHolder;

public class SnapMessageHolder extends MessageHolder implements MessageContentType {

    public String customValue;

    public SnapMessageHolder(Message message) {
        super(message);
        customValue = (String) message.valueForKey("Key");
    }
}
