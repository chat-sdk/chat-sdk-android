package firefly.sdk.chat.chat;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import firefly.sdk.chat.message.DeliveryReceipt;
import firefly.sdk.chat.message.Invitation;
import firefly.sdk.chat.message.Message;
import firefly.sdk.chat.message.Presence;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.message.TypingState;
import firefly.sdk.chat.namespace.FireflyMessage;
import firefly.sdk.chat.firebase.rx.MultiQueueSubject;

public class Events {

    protected MultiQueueSubject<Message> messages = MultiQueueSubject.create();
    protected MultiQueueSubject<DeliveryReceipt> deliveryReceipts = MultiQueueSubject.create();
    protected MultiQueueSubject<TypingState> typingStates = MultiQueueSubject.create();
    protected MultiQueueSubject<Presence> presences = MultiQueueSubject.create();
    protected MultiQueueSubject<Invitation> invitations = MultiQueueSubject.create();

    protected MultiQueueSubject<Sendable> sendables = MultiQueueSubject.create();

    protected PublishSubject<Throwable> errors = PublishSubject.create();


    public MultiQueueSubject<Message> getMessages() {
        return messages;
    }

    /**
     * A Firefly Message is no different from a Message. The reason this method
     * exists is because Message is a very common class name. If for any reason
     * your project already has a Message object, you can use the FireflyMessage
     * to avoid a naming clash
     * @return events of messages
     */
    public Observable<FireflyMessage> getFireflyMessages() {
        return messages.map(FireflyMessage::fromMessage).hide();
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

    public MultiQueueSubject<Sendable> getSendables() {
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
