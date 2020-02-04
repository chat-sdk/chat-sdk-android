package co.chatsdk.firestream;

import org.joda.time.DateTime;

import java.util.HashMap;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import firestream.chat.message.Sendable;
import io.reactivex.Single;

public class FirestreamHelper {

    public static Single<Message> sendableToMessage(Sendable sendable) {
        return Single.defer(() -> {
            return ChatSDK.core().getUserForEntityID(sendable.getFrom()).map(user -> {

                Message message = ChatSDK.db().fetchEntityWithEntityID(sendable.getId(), Message.class);
                if (message != null) {
                    return message;
                }

                message = ChatSDK.db().createEntity(Message.class);

                message.setSender(user);
                message.setMessageStatus(MessageSendStatus.Sent);

                copyToMessage(message, sendable);

                return message;
            });
        });
    }

    public static void copyToMessage(Message message, Sendable sendable) {
        message.setDate(new DateTime(sendable.getDate()));
        message.setEntityID(sendable.getId());

        HashMap<String, Object> body = sendable.getBody();

        Object metaObject = body.get(Keys.Meta);
        if (metaObject instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> meta = new HashMap<>((HashMap) metaObject);
            message.setMetaValues(meta);
        }

        Object typeObject = body.get(Keys.Type);

        if (typeObject instanceof Long) {
            Integer type = ((Long) typeObject).intValue();
            message.setType(type);
        }
        if (typeObject instanceof Integer) {
            Integer type = (Integer) typeObject;
            message.setType(type);
        }

        message.update();

    }
}
