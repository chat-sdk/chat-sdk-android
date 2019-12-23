package sdk.chat.micro.chat;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.micro.message.DeliveryReceipt;
import sdk.chat.micro.message.Invitation;
import sdk.chat.micro.message.Message;
import sdk.chat.micro.message.Presence;
import sdk.chat.micro.message.Sendable;
import sdk.chat.micro.message.TypingState;
import sdk.chat.micro.namespace.MicroMessage;
import sdk.chat.micro.rx.MultiQueueSubject;

public class Events {

    protected MultiQueueSubject<Message> messages = MultiQueueSubject.create();
    protected MultiQueueSubject<DeliveryReceipt> deliveryReceipts = MultiQueueSubject.create();
    protected MultiQueueSubject<TypingState> typingStates = MultiQueueSubject.create();
    protected MultiQueueSubject<Presence> presences = MultiQueueSubject.create();
    protected MultiQueueSubject<Invitation> invitations = MultiQueueSubject.create();

    protected MultiQueueSubject<Sendable> sendables = MultiQueueSubject.create();

    protected PublishSubject<Throwable> errors = PublishSubject.create();

    public Observable<Throwable> getErrors() {
        return errors.hide();
    }

    public MultiQueueSubject<Message> getMessages() {
        return messages;
    }

    /**
     * A Micro Message is no different from a Message. The reason this method
     * exists is because Message is a very common class name. If for any reason
     * your project already has a Message object, you can use the MicroMessage
     * to avoid a naming clash
     * @return events of messages
     */
    public Observable<MicroMessage> getMicroMessages() {
        return messages.map(MicroMessage::fromMessage).hide();
    }

    public MultiQueueSubject<DeliveryReceipt> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public MultiQueueSubject<TypingState> getTypingStates() {
        return typingStates;
    }

    public MultiQueueSubject<Sendable> getSendables() {
        return sendables;
    }


    public MultiQueueSubject<Presence> getPresences() {
        return presences;
    }
    public MultiQueueSubject<Invitation> getInvitations() {
        return invitations;
    }

    public PublishSubject<Throwable> impl_throwablePublishSubject() {
        return errors;
    }

}
