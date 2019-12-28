package firefly.sdk.chat.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import firefly.sdk.chat.Config;
import firefly.sdk.chat.events.ListEvent;
import firefly.sdk.chat.firebase.service.Path;
import firefly.sdk.chat.message.DeliveryReceipt;
import firefly.sdk.chat.message.Invitation;
import firefly.sdk.chat.message.Message;
import firefly.sdk.chat.message.Presence;
import firefly.sdk.chat.message.Sendable;
import firefly.sdk.chat.message.TypingState;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.firebase.rx.DisposableList;
import firefly.sdk.chat.types.SendableType;

/**
 * This class handles common elements of a conversation bit it 1-to-1 or group.
 * Mainly sending and receiving messages.
 */
public abstract class AbstractChat implements Consumer<Throwable> {

    /**
     * Store the disposables so we can dispose of all of them when the user logs out
     */
    protected DisposableList dl = new DisposableList();

    /**
     * Event events
     */
    protected Events events = new Events();

    /**
     * A list of all sendables received
     */
    protected ArrayList<Sendable> sendables = new ArrayList<>();

    /**
     * Current configuration
     */
    protected Config config = new Config();

    /**
     * Error handler method so we can redirect all errors to the error events
     * @param throwable - the events error
     * @throws Exception
     */
    @Override
    public void accept(Throwable throwable) throws Exception {
        events.errors.onError(throwable);
    }

    /**
     * Start listening to the current message reference and retrieve all messages
     * @return a events of message results
     */
    protected Observable<Sendable> messagesOn() {
        return messagesOn(null);
    }

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    protected Observable<Sendable> messagesOn(Date newerThan) {
        return Fl.y.getFirebaseService().core.messagesOn(messagesPath(), newerThan, config.messageHistoryLimit).doOnNext(sendable -> {
            if (sendable != null) {
                getEvents().getSendables().onNext(sendable);
                sendables.add(sendable);
            }
        }).doOnError(throwable -> {
            events.publishThrowable().onNext(throwable);
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get a updateBatch of messages once
     * @param fromDate get messages from this date
     * @param toDate get messages until this date
     * @param limit limit the maximum number of messages
     * @return a events of message results
     */
    public Observable<Sendable> messagesOnce(@Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Fl.y.getFirebaseService().core
                .messagesOnce(messagesPath(), fromDate, toDate, limit)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * This method gets the date of the last delivery receipt that we sent - i.e. the
     * last message WE received.
     * @return single date
     */
    protected Single<Date> dateOfLastDeliveryReceipt() {
        return Fl.y.getFirebaseService().core
                .dateOfLastSentMessage(messagesPath())
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Listen for changes in the value of a list reference
     * @param path to listen to
     * @return events of list events
     */
    protected Observable<ListEvent> listChangeOn(Path path) {
        return Fl.y.getFirebaseService().core
                .listChangeOn(path)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable send(Path messagesPath, Sendable sendable) {
        return send(messagesPath, sendable, null);
    }

        /**
         * Send a message to a messages ref
         * @param messagesPath
         * @param sendable item to be sent
         * @param newId the ID of the new message
         * @return single containing message id
         */
    public Completable send(Path messagesPath, Sendable sendable, @Nullable Consumer<String> newId) {
        return Fl.y.getFirebaseService().core
                .send(messagesPath, sendable, newId)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Delete a sendable from our queue
     * @param messagesPath
     * @return completion
     */
    protected Completable deleteSendable (Path messagesPath) {
        return Fl.y.getFirebaseService().core
                .deleteSendable(messagesPath)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Remove a user from a reference
     * @param path for users
     * @param user to remove
     * @return completion
     */
    protected Completable removeUser(Path path, User user) {
        return removeUsers(path, user);
    }

    /**
     * Remove users from a reference
     * @param path for users
     * @param users to remove
     * @return completion
     */
    protected Completable removeUsers(Path path, User... users) {
        return removeUsers(path, Arrays.asList(users));
    }

    /**
     * Remove users from a reference
     * @param path for users
     * @param users to remove
     * @return completion
     */
    protected Completable removeUsers(Path path, List<User> users) {
        return Fl.y.getFirebaseService().core
                .removeUsers(path, users)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Add a user to a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param user to add
     * @return completion
     */
    protected Completable addUser(Path path, User.DataProvider dataProvider, User user) {
        return addUsers(path, dataProvider, user);
    }

    /**
     * Add users to a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to add
     * @return completion
     */
    public Completable addUsers(Path path, User.DataProvider dataProvider, User... users) {
        return addUsers(path, dataProvider, Arrays.asList(users));
    }

    /**
     * Add users to a reference
     * @param path
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to add
     * @return completion
     */
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return Fl.y.getFirebaseService().core
                .addUsers(path, dataProvider, users)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates a user for a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param user to update
     * @return completion
     */
    public Completable updateUser(Path path, User.DataProvider dataProvider, User user) {
        return updateUsers(path, dataProvider, user);
    }

    /**
     * Update users for a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to update
     * @return completion
     */
    public Completable updateUsers(Path path, User.DataProvider dataProvider, User... users) {
        return updateUsers(path, dataProvider, Arrays.asList(users));
    }

    /**
     * Update users for a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to update
     * @return completion
     */
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<User> users) {
        return Fl.y.getFirebaseService().core
                .updateUsers(path, dataProvider, users)
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Connect to the chat
     * @throws Exception error if we are not connected
     */
    public void connect() throws Exception {
        dl.add(dateOfLastDeliveryReceipt()
                .flatMapObservable(this::messagesOn)
                .subscribe(this::passMessageResultToStream, this));
    }

    /**
     * Disconnect from a chat
     */
    public void disconnect () {
        dl.dispose();
    }

    /**
     * Convenience method to cast sendables and send them to the correct events
     * @param sendable the base sendable
     */
    protected void passMessageResultToStream(Sendable sendable) {

        System.out.println("Sendable: " + sendable.type + " " + sendable.id);

        if (sendable.type.equals(SendableType.Message)) {
            events.getMessages().onNext(Message.fromSendable(sendable));
        }
        if (sendable.type.equals(SendableType.DeliveryReceipt)) {
            events.getDeliveryReceipts().onNext(DeliveryReceipt.fromSendable(sendable));
        }
        if (sendable.type.equals(SendableType.TypingState)) {
            events.getTypingStates().onNext(TypingState.fromSendable(sendable));
        }
        if (sendable.type.equals(SendableType.Invitation)) {
            events.getInvitations().onNext(Invitation.fromSendable(sendable));
        }
        if (sendable.type.equals(SendableType.Presence)) {
            events.getPresences().onNext(Presence.fromSendable(sendable));
        }

    }

    /**
     * returns the events object which exposes the different sendable streams
     * @return events
     */
    public Events getEvents() {
        return events;
    }

    /**
     * Overridable messages reference
     * @return Firestore messages reference
     */
    protected abstract Path messagesPath();

}
