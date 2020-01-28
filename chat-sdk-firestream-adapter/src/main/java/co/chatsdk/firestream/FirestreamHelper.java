package co.chatsdk.firestream;

import android.net.Network;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.concurrent.Callable;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import firestream.chat.message.Sendable;
import io.reactivex.Single;
import io.reactivex.SingleSource;

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

//                ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

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
