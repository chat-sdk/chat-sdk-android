package firestream.chat.chat;

import firestream.chat.firebase.rx.MultiQueueSubject;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Invitation;
import firestream.chat.message.Message;
import firestream.chat.message.Presence;
import firestream.chat.message.Sendable;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.FireStreamMessage;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import sdk.guru.common.Event;

public class Events {

    protected MultiQueueSubject<Event<Message>> messages = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<DeliveryReceipt>> deliveryReceipts = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<TypingState>> typingStates = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<Presence>> presences = MultiQueueSubject.create();
    protected MultiQueueSubject<Event<Invitation>> invitations = MultiQueueSubject.create();

    /**
     * The sendable event stream provides the most information. It passes a sendable event
     * when will include the kind of action that has been performed.
     */
    protected MultiQueueSubject<Event<Sendable>> sendables = MultiQueueSubject.create();

    protected PublishSubject<Throwable> errors = PublishSubject.create();


    /**
     * Note: when you send a message, that will trigger both a message added event and a
     * message updated event. As soon as the message is added to the Firebase cache, the
     * message added event will be triggered and the message will have an estimated time
     * stamp. Then when the message has been written to the server, it will be updated
     * with the server timestamp.
     * @return
     */
    public MultiQueueSubject<Event<Message>> getMessages() {
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

    public MultiQueueSubject<Event<DeliveryReceipt>> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public MultiQueueSubject<Event<TypingState>> getTypingStates() {
        return typingStates;
    }

    public MultiQueueSubject<Event<Sendable>> getSendables() {
        return sendables;
    }


    public MultiQueueSubject<Event<Presence>> getPresences() {
        return presences;
    }
    public MultiQueueSubject<Event<Invitation>> getInvitations() {
        return invitations;
    }

    public PublishSubject<Throwable> publishThrowable() {
        return errors;
    }

}
