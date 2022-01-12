package sdk.chat.dcom;

import io.reactivex.Completable;
import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.firebase.adapter.FirebaseThreadHandler;
import sdk.chat.firebase.adapter.module.FirebaseModule;

public class DComThreadHandler extends FirebaseThreadHandler {

    @Override
    public boolean canDeleteMessage(Message message) {

        // We do it this way because otherwise when we exceed the number of messages,
        // This event is triggered as the messages go out of scope
        if (message.getDate().getTime() < message.getThread().getCanDeleteMessagesFrom().getTime()) {
            return false;
        }

        return true;
    }

    @Override
    public Completable deleteMessage(Message message) {
        return Completable.defer(() -> {
            if (((message.getMessageStatus().equals(MessageSendStatus.Sent) && message.getSender().isMe()) || !message.getSender().isMe()) && !message.getMessageType().is(MessageType.System)) {
                return FirebaseModule.config().provider.messageWrapper(message).delete();
            }
            return Completable.complete();
        });
    }

}
