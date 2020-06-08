package firestream.chat.chat;

import androidx.annotation.Nullable;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import firestream.chat.events.ListData;
import firestream.chat.filter.Filter;
import firestream.chat.firebase.service.Path;
import firestream.chat.interfaces.IAbstractChat;
import firestream.chat.message.Sendable;
import firestream.chat.message.TypingState;
import firestream.chat.namespace.Fire;
import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;
import firestream.chat.util.TypingMap;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

/**
 * This class handles common elements of a conversation bit it 1-to-1 or group.
 * Mainly sending and receiving messages.
 */
public abstract class AbstractChat implements IAbstractChat {

    /**
     * Store the disposables so we can dispose of all of them when the user logs out
     */
    protected DisposableMap dm = new DisposableMap();

    /**
     * Event events
     */
    protected Events events = new Events();

    /**
     * A list of all sendables received
     */
    protected List<Sendable> sendables = new ArrayList<>();

    protected TypingMap typingMap = new TypingMap();

    /**
     * Start listening to the current message reference and retrieve all messages
     * @return a events of message results
     */
    protected Observable<Event<Sendable>> messagesOn() {
        return messagesOn(null);
    }

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    protected Observable<Event<Sendable>> messagesOn(Date newerThan) {
        return Fire.internal().getFirebaseService().core.messagesOn(messagesPath(), newerThan).doOnNext(event -> {
            Sendable sendable = event.get();

            Sendable previous = getSendable(sendable.getId());
            if (event.isAdded()) {
                sendables.add(sendable);
            }
            if (previous != null) {
                if (event.isModified()) {
                    sendable.copyTo(previous);
                }
                if (event.isRemoved()) {
                    sendables.remove(previous);
                }
            }
            getSendableEvents().getSendables().onNext(event);
        }).doOnError(throwable -> {
            events.publishThrowable().onNext(throwable);
        }).observeOn(RX.main());
    }

    /**
     * Get a updateBatch of messages once
     * @param fromDate get messages from this date
     * @param toDate get messages until this date
     * @param limit limit the maximum number of messages
     * @return a events of message results
     */
    protected Single<List<Sendable>> loadMoreMessages(@Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit, boolean desc) {
        return Fire.stream().getFirebaseService().core
                .loadMoreMessages(messagesPath(), fromDate, toDate, limit)
                .map(sendables -> {
                    Collections.sort(sendables, (s1, s2) -> {
                        if (desc) {
                            return s2.getDate().compareTo(s1.getDate());
                        } else {
                            return s1.getDate().compareTo(s2.getDate());
                        }
                    });
                return sendables;
                })
                .observeOn(RX.main());
    }

    public Single<List<Sendable>> loadMoreMessages(Date fromDate, Date toDate, boolean desc) {
        return loadMoreMessages(fromDate, toDate, null, desc);
    }

    public Single<List<Sendable>> loadMoreMessagesFrom(Date fromDate, Integer limit, boolean desc) {
        return loadMoreMessages(fromDate, null, limit, desc);
    }

    public Single<List<Sendable>> loadMoreMessagesTo(Date toDate, Integer limit, boolean desc) {
        return loadMoreMessages(null, toDate, limit, desc);
    }

    public Single<List<Sendable>> loadMoreMessagesBefore(final Date toDate, Integer limit, boolean desc) {
        return Single.defer(() -> {
            Date before = toDate == null ? null : new Date(toDate.getTime() - 1);
            return loadMoreMessagesTo(before, limit, desc);
        });
    }

    /**
     * Listen for changes in the value of a list reference
     * @param path to listen to
     * @return events of list events
     */
    protected Observable<Event<ListData>> listChangeOn(Path path) {
        return Fire.stream().getFirebaseService().core
                .listChangeOn(path)
                .observeOn(RX.main());
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
        return Fire.stream().getFirebaseService().core
                .send(messagesPath, sendable, newId)
                .observeOn(RX.main());
    }

    /**
     * Delete a sendable from our queue
     * @param messagesPath
     * @return completion
     */
    protected Completable deleteSendable (Path messagesPath) {
        return Fire.stream().getFirebaseService().core
                .deleteSendable(messagesPath)
                .observeOn(RX.main());
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
    protected Completable removeUsers(Path path, List<? extends User> users) {
        return Fire.stream().getFirebaseService().core
                .removeUsers(path, users)
                .observeOn(RX.main());
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
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Fire.stream().getFirebaseService().core
                .addUsers(path, dataProvider, users)
                .observeOn(RX.main());
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
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Fire.stream().getFirebaseService().core
                .updateUsers(path, dataProvider, users)
                .observeOn(RX.main());
    }

    @Override
    public void connect() throws Exception {
        dm.add(Single.defer((Callable<SingleSource<Optional<Sendable>>>) () -> {
            // If we are deleting the messages on receipt then we want to get all new messages
            if (Fire.stream().getConfig().deleteMessagesOnReceipt) {
                return Single.just(Optional.empty());
            } else {
                return Fire.stream().getFirebaseService().core.lastMessage(messagesPath());
            }
        }).flatMapObservable((Function<Optional<Sendable>, ObservableSource<Event<Sendable>>>) optional -> {

            Date date = null;
            if (!optional.isEmpty()) {
                if (Fire.stream().getConfig().emitEventForLastMessage) {
                    passMessageResultToStream(new Event<>(optional.get(), EventType.Added));
                }
                date = optional.get().getDate();
            }

            return messagesOn(date);
        }).subscribe(this::passMessageResultToStream, this));
    }

    @Override
    public void disconnect () {
        dm.dispose();
    }

    /**
     * Convenience method to cast sendables and send them to the correct events
     * @param event sendable event
     */
    protected void passMessageResultToStream(Event<Sendable> event) {
        Sendable sendable = event.get();

        debug("Sendable: " + sendable.getType() + " " + sendable.getId() + ", date: " + sendable.getDate().getTime());

        // In general, we are mostly interested when messages are added
        if (sendable.isType(SendableType.message())) {
            events.getMessages().onNext(event.to(sendable.toMessage()));
        }
        if (sendable.isType(SendableType.deliveryReceipt())) {
            events.getDeliveryReceipts().onNext(event.to(sendable.toDeliveryReceipt()));
        }
        if (sendable.isType(SendableType.typingState())) {
            TypingState typingState = sendable.toTypingState();
            if (event.isAdded()) {
                typingState.setBodyType(TypingStateType.typing());
            }
            if (event.isRemoved()) {
                typingState.setBodyType(TypingStateType.none());
            }
            events.getTypingStates().onNext(new Event<>(typingState, EventType.Modified));
        }
        if (sendable.isType(SendableType.invitation())) {
            events.getInvitations().onNext(event.to(sendable.toInvitation()));
        }
        if (sendable.isType(SendableType.presence())) {
            events.getPresences().onNext(event.to(sendable.toPresence()));
        }
    }

    @Override
    public List<Sendable> getSendables() {
        return sendables;
    }

    @Override
    public List<Sendable> getSendables(SendableType type) {
        List<Sendable> sendables = new ArrayList<>();
        for (Sendable s: sendables) {
            if (s.isType(type)) {
                sendables.add(s);
            }
        }
        return sendables;
    }

//    public DeliveryReceipt getDeliveryReceiptsForMessage(String messageId, DeliveryReceiptType type) {
//        List<DeliveryReceipt> receipts = getSendables(DeliveryReceipt.class);
//        for (DeliveryReceipt receipt: receipts) {
//            try {
//                if (receipt.getMessageId().equals(messageId) && receipt.getDeliveryReceiptType() == type) {
//                    return receipt;
//                }
//            } catch (Exception ignored) {}
//        }
//        return null;
//    }

    public <T extends Sendable> List<T> getSendables(Class<T> clazz) {
        return new Sendable.Converter<T>(clazz).convert(getSendables());
    }

    @Override
    public Sendable getSendable(String id) {
        for (Sendable s: sendables) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * returns the events object which exposes the different sendable streams
     * @return events
     */
    public Events getSendableEvents() {
        return events;
    }

    /**
     * Overridable messages reference
     * @return Firestore messages reference
     */
    protected abstract Path messagesPath();

    @Override
    public DisposableMap getDisposableMap() {
        return dm;
    }

    @Override
    public void manage(Disposable disposable) {
        getDisposableMap().add(disposable);
    }

    public abstract Completable markRead(Sendable message);
    public abstract Completable markReceived(Sendable message);

    public void debug(String text) {
        if (Fire.stream().getConfig().debugEnabled) {
            Logger.debug(text);
        }
    }

    protected Predicate<Event<? extends Sendable>> deliveryReceiptFilter() {
        return Filter.and(new ArrayList<Predicate<Event<? extends Sendable>>>() {{
            add(Fire.internal().getMarkReceivedFilter());
            add(Filter.notFromMe());
            add(Filter.byEventType(EventType.Added));
        }});
    }

    @Override
    public void onSubscribe(Disposable d) {
        dm.add(d);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable e) {
        events.publishThrowable().onNext(e);
    }

    /**
     * Error handler method so we can redirect all errors to the error events
     * @param throwable - the events error
     */
    @Override
    public void accept(Throwable throwable) {
        onError(throwable);
    }


}
