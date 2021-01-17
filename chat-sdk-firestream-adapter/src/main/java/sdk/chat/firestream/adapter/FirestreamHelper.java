package sdk.chat.firestream.adapter;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import firestream.chat.message.Body;
import firestream.chat.message.Sendable;
import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;

public class FirestreamHelper {

    public static Single<Message> sendableToMessage(final Thread thread, Sendable sendable, boolean notify) {
        return Single.defer(() -> {
            return ChatSDK.core().getUserForEntityID(sendable.getFrom()).map(user -> {

                Message message = ChatSDK.db().fetchEntityWithEntityID(sendable.getId(), Message.class);
                if (message != null) {
                    return message;
                }

                message = ChatSDK.db().createEntity(Message.class);

                message.setSender(user);

//                message.setMessageStatus(MessageSendStatus.Sent);

                copyToMessage(message, sendable);
                thread.addMessage(message, notify);

                return message;
            }).flatMap(message -> {
                if (!message.getSender().isMe()) {
                    return message.setUserReadStatusAsync(ChatSDK.currentUser(), ReadStatus.delivered(), new Date(), false).map(aBoolean -> message);
                }
                return Single.just(message);
            });
        });
    }

    public static void copyToMessage(Message message, Sendable sendable) {
        message.setDate(sendable.getDate());
        message.setEntityID(sendable.getId());

        Body body = sendable.getBody();

        Object metaObject = body.get(Keys.Meta);
        if (metaObject instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> meta = new HashMap<>((Map) metaObject);
            message.setMetaValues(meta);
        }

        String typeString = body.getType().get();
        int type = Integer.parseInt(typeString);
        message.setType(type);

        message.update();

    }
}
