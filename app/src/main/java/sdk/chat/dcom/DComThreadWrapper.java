package sdk.chat.dcom;

import java.util.HashMap;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;

public class DComThreadWrapper extends ThreadWrapper {

    public DComThreadWrapper(ThreadX thread) {
        super(thread);
    }

    public DComThreadWrapper(String entityId) {
        super(entityId);
    }

    public void removeMessage(Message message) {

        boolean reloadAll = !message.typeIs(MessageType.Text);

        message.setText("Message Deleted");
        message.setType(MessageType.Text);
        message.update();

        NetworkEvent event = NetworkEvent.messageUpdated(message);
        if (reloadAll) {
            event.setData(new HashMap<String, Object>() {{
                put(DCom.reloadData, true);
            }});
        }
        ChatSDK.events().source().accept(event);
    }
}
