package sdk.chat.core.dao;

import org.joda.time.DateTime;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.RX;

public class MessageAsync {

    public static void markRead(Message message) {
        if (ChatSDK.readReceipts() != null) {
            ChatSDK.readReceipts().markRead(message);
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
            message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.delivered(), new DateTime());
        });
    }
}
