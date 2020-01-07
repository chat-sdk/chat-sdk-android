package firestream.chat.chat;

import firestream.chat.events.SendableEvent;
import firestream.chat.filter.MessageStreamFilter;
import firestream.chat.types.SendableType;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import firestream.chat.message.DeliveryReceipt;
import firestream.chat.message.Invitation;
import firestream.chat.message.Message;
import firestream.chat.message.Presence;
import firestream.chat.message.Sendable;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.FirestreamMessage;
import firestream.chat.firebase.rx.MultiQueueSubject;

public class Events {

    protected MultiQueueSubject<Message> messages = MultiQueueSubject.create();
    protected MultiQueueSubject<DeliveryReceipt> deliveryReceipts = MultiQueueSubject.create();
    protected MultiQueueSubject<TypingState> typingStates = MultiQueueSubject.create();
    protected MultiQueueSubject<Presence> presences = MultiQueueSubject.create();
    protected MultiQueueSubject<Invitation> invitations = MultiQueueSubject.create();

    /**
     * The sendable event stream provides the most information. It passes a sendable event
     * when will include the kind of action that has been performed.
     */
    protected MultiQueueSubject<SendableEvent> sendables = MultiQueueSubject.create();

    protected PublishSubject<Throwable> errors = PublishSubject.create();


    public MultiQueueSubject<Message> getMessages() {
        return messages;
    }

    /**
     * A FireStream Message isType no different from a Message. The reason this method
     * exists isType because Message isType a very common class name. If for any reason
     * your project already has a Message object, you can use the FirestreamMessage
     * to avoid a naming clash
     * @return events of messages
     */
    public Observable<FirestreamMessage> getFireStreamMessages() {
        return messages
                .map(FirestreamMessage::fromMessage)
                .hide();
    }

    /**
     * Get a stream of errors from the chat
     * @return
     */
    public Observable<Throwable> getErrors() {
        return errors.hide();
    }

    public MultiQueueSubject<DeliveryReceipt> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public MultiQueueSubject<TypingState> getTypingStates() {
        return typingStates;
    }

    public MultiQueueSubject<SendableEvent> getSendables() {
        return sendables;
    }


    public MultiQueueSubject<Presence> getPresences() {
        return presences;
    }
    public MultiQueueSubject<Invitation> getInvitations() {
        return invitations;
    }

    public PublishSubject<Throwable> publishThrowable() {
        return errors;
    }

}
