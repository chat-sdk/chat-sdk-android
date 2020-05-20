package sdk.chat.core.dao;



import java.util.Date;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.RX;

public class MessageAsync {

    public static void markRead(Message message) {
        if (ChatSDK.readReceipts() != null) {
            ChatSDK.readReceipts().markRead(message);
        } else {
            RX.onBackground(() -> message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date(), false));
        }
    }

    public static Single<Boolean> isRead(Message message) {
        return Single.defer(() -> Single.just(message.isRead())).subscribeOn(RX.db());
    }

    public static void markReadIfNecessaryAsync(Message message) {
        isRead(message).doOnSuccess(isRead -> {
            if (!isRead) {
                markRead(message);
            }
        }).ignoreElement().subscribe(ChatSDK.events());
    }

    public static void markDelivered(Message message) {
        RX.onBackground(() -> {
            message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.delivered(), new Date());
        });
    }

    public static Single<Boolean> setUserReadStatusAsync(Message message, User user, ReadStatus status, Date date, boolean notify) {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            emitter.onSuccess(message.setUserReadStatus(user, status, date, notify));
        }).subscribeOn(RX.single());
    }

}
