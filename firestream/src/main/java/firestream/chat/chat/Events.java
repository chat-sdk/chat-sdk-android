package firestream.chat.chat;

import com.jakewharton.rxrelay2.PublishRelay;

import firestream.chat.firebase.rx.MultiRelay;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Invitation;
import firestream.chat.message.Message;
import firestream.chat.message.Presence;
import firestream.chat.message.Sendable;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.FireStreamMessage;
import io.reactivex.Observable;
import sdk.guru.common.Event;

public class Events {

    protected MultiRelay<Event<Message>> messages = MultiRelay.create();
    protected MultiRelay<Event<DeliveryReceipt>> deliveryReceipts = MultiRelay.create();
    protected MultiRelay<Event<TypingState>> typingStates = MultiRelay.create();
    protected MultiRelay<Event<Presence>> presences = MultiRelay.create();
    protected MultiRelay<Event<Invitation>> invitations = MultiRelay.create();

    /**
     * The sendable event stream provides the most information. It passes a sendable event
     * when will include the kind of action that has been performed.
     */
    protected MultiRelay<Event<Sendable>> sendables = MultiRelay.create();

    protected PublishRelay<Throwable> errors = PublishRelay.create();


    /**
     * Note: when you send a message, that will trigger both a message added event and a
     * message updated event. As soon as the message is added to the Firebase cache, the
     * message added event will be triggered and the message will have an estimated time
     * stamp. Then when the message has been written to the server, it will be updated
     * with the server timestamp.
     * @return
     */
    public MultiRelay<Event<Message>> getMessages() {
        return messages;
    }

    /**
     * A FireStream Message type no different from a Message. The reason this method
     * exists type because Message type a very common class name. If for any reason
     * your project already has a Message object, you can use the FireStreamMessage
     * to avoid a naming clash
     * @return events of messages
     */
    public Observable<Event<FireStreamMessage>> getFireStreamMessages() {
        return messages.map(messageEvent -> messageEvent.to(FireStreamMessage.fromMessage(messageEvent.get()))).hide();
    }

    /**
     * Get a stream of errors from the chat
     * @return
     */
    public Observable<Throwable> getErrors() {
        return errors.hide();
    }

    public MultiRelay<Event<DeliveryReceipt>> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public MultiRelay<Event<TypingState>> getTypingStates() {
        return typingStates;
    }

    public MultiRelay<Event<Sendable>> getSendables() {
        return sendables;
    }


    public MultiRelay<Event<Presence>> getPresences() {
        return presences;
    }
    public MultiRelay<Event<Invitation>> getInvitations() {
        return invitations;
    }

    public PublishRelay<Throwable> publishThrowable() {
        return errors;
    }

}
