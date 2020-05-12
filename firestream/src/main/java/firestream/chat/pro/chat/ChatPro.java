package firestream.chat.pro.chat;

import androidx.annotation.Nullable;

import java.util.Date;

import firestream.chat.chat.Chat;
import firestream.chat.chat.Meta;
import firestream.chat.chat.User;
import firestream.chat.firebase.service.Paths;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.Fire;
import firestream.chat.pro.interfaces.IChatPro;
import firestream.chat.pro.message.DeliveryReceipt;
import firestream.chat.pro.message.TypingState;
import firestream.chat.pro.types.DeliveryReceiptType;
import firestream.chat.pro.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.functions.Consumer;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;

public class ChatPro extends Chat implements IChatPro {
    public ChatPro(String id) {
        super(id);
    }

    public ChatPro(String id, Date joined, Meta meta) {
        super(id, joined, meta);
    }

    public ChatPro(String id, Date joined) {
        super(id, joined);
    }

    @Override
    public void connect() throws Exception {

        // If delivery receipts are enabled, send the delivery receipt
        if (Fire.internal().getConfig().deliveryReceiptsEnabled) {
            getSendableEvents()
                    .getMessages()
                    .pastAndNewEvents()
                    .filter(deliveryReceiptFilter())
                    .flatMapCompletable(messageEvent -> markReceived(messageEvent.get()))
                    .subscribe(this);
        }

        super.connect();
    }

    @Override
    public Completable markReceived(Sendable sendable) {
        return markReceived(sendable.getId());
    }

    @Override
    public Completable markReceived(String sendableId) {
        return sendDeliveryReceipt(DeliveryReceiptType.received(), sendableId);
    }

    @Override
    public Completable markRead(Sendable sendable) {
        return markRead(sendable.getId());
    }

    @Override
    public Completable markRead(String sendableId) {
        return sendDeliveryReceipt(DeliveryReceiptType.read(), sendableId);
    }

//    public abstract Completable markRead(Sendable message);
//    public abstract Completable markReceived(Sendable message);

    @Override
    public Completable sendTypingIndicator(TypingStateType type) {
        return sendTypingIndicator(type, null);
    }

    @Override
    public Completable sendTypingIndicator(TypingStateType type, @Nullable Consumer<String> newId) {
        return send(new TypingState(type), newId);
    }

    @Override
    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId) {
        return sendDeliveryReceipt(type, messageId, null);
    }

    @Override
    public Completable sendDeliveryReceipt(DeliveryReceiptType type, String messageId, @Nullable Consumer<String> newId) {
        return send(new DeliveryReceipt(type, messageId), newId);
    }
}
