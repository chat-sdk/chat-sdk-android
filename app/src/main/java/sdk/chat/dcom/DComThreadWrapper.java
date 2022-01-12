package sdk.chat.dcom;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;

public class DComThreadWrapper extends ThreadWrapper {

    public DComThreadWrapper(Thread thread) {
        super(thread);
    }

    public DComThreadWrapper(String entityId) {
        super(entityId);
    }

    public void removeMessage(Message message) {
        message.setType(MessageType.Text);
        message.setText("Message Deleted");

        ChatSDK.events().source().accept(NetworkEvent.messageUpdated(message));
    }
}
