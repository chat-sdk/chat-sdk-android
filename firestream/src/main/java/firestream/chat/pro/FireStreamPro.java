package firestream.chat.pro;

import java.util.Date;

import javax.annotation.Nullable;

import firefly.sdk.chat.R;
import firestream.chat.FireStream;
import firestream.chat.chat.Chat;
import firestream.chat.chat.User;
import firestream.chat.events.ConnectionEvent;
import firestream.chat.filter.Filter;
import firestream.chat.firebase.service.Keys;
import firestream.chat.firebase.service.Paths;
import firestream.chat.interfaces.IChat;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.Fire;
import firestream.chat.pro.interfaces.IFirestreamPro;
import firestream.chat.pro.message.DeliveryReceipt;
import firestream.chat.pro.message.Presence;
import firestream.chat.pro.message.TypingState;
import firestream.chat.pro.types.DeliveryReceiptType;
import firestream.chat.pro.types.PresenceType;
import firestream.chat.pro.types.TypingStateType;
import firestream.chat.types.SendableType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;

public class FireStreamPro extends FireStream implements IFirestreamPro {

    public static final FireStreamPro instance = new FireStreamPro();
    public static FireStreamPro shared () {
        return instance;
    }

    public void connect () throws Exception {
        super.connect();

        // DELIVERY RECEIPTS

        getSendableEvents()
                .getMessages()
                .pastAndNewEvents()
                .filter(deliveryReceiptFilter())
                .flatMapCompletable(event -> markReceived(event.get()))
                .subscribe(this);

        // If message deletion is disabled, send a received receipt to ourself for each message. This means
        // that when we add a childListener, we only get new messages
        if (!config.deleteMessagesOnReceipt && config.startListeningFromLastSentMessageDate) {
            getSendableEvents()
                    .getMessages()
                    .pastAndNewEvents()
                    .filter(Filter.notFromMe())
                    .flatMapCompletable(event -> {

                        return sendDeliveryReceipt(currentUserId(), DeliveryReceiptType.received(), event.get().getId());

                    }).subscribe(this);
        }

    }

    @Override
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(userId, type, messageId, null);
    }

    @Override
    public Completable sendDeliveryReceipt(String userId, DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(userId, new DeliveryReceipt(type, messageId), newId);
    }

    @Override
    public Completable sendTypingIndicator(String userId, TypingStateType type) {
        return sendTypingIndicator(userId, type, null);
    }

    @Override
    public Completable sendTypingIndicator(String userId, TypingStateType type, @Nullable Consumer<String> newId) {
        return send(userId, new TypingState(type), newId);
    }

    @Override
    public Completable sendPresence(String userId, PresenceType type) {
        return sendPresence(userId, type, null);
    }

    @Override
    public Completable sendPresence(String userId, PresenceType type, @Nullable Consumer<String> newId) {
        return send(userId, new Presence(type), newId);
    }

    /**
     * Send a read receipt
     * @return completion
     */
    public Completable markRead(Sendable sendable) {
        return markRead(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markRead(String fromUserId, String sendableId) {
        return Fire.pro().sendDeliveryReceipt(fromUserId, DeliveryReceiptType.read(), sendableId);
    }

    /**
     * Send a received receipt
     * @return completion
     */
    public Completable markReceived(Sendable sendable) {
        return markReceived(sendable.getFrom(), sendable.getId());
    }

    @Override
    public Completable markReceived(String fromUserId, String sendableId) {
        return Fire.pro().sendDeliveryReceipt(fromUserId, DeliveryReceiptType.received(), sendableId);
    }
}
