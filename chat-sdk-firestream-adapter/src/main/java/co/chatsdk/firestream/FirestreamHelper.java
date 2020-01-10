package co.chatsdk.firestream;

import org.joda.time.DateTime;

import java.util.HashMap;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import firestream.chat.message.Sendable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class FirestreamHelper {

    public static Single<Message> sendableToMessage(Sendable sendable) {
        return APIHelper.fetchRemoteUser(sendable.getFrom()).map(user -> {
            Message message = ChatSDK.db().createEntity(Message.class);

            message.setSender(user);
            message.setMessageStatus(MessageSendStatus.Delivered);
            message.setDate(new DateTime(sendable.getDate()));
            message.setEntityID(sendable.getId());

            HashMap<String, Object> body = sendable.getBody();

            Object metaObject = body.get(Keys.Meta);
            if (metaObject instanceof HashMap) {
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
            return message;            });
    }

}
