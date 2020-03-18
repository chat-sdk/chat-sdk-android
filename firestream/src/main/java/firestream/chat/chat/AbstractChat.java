package firestream.chat.chat;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import firestream.chat.events.Event;
import firestream.chat.events.EventType;
import firestream.chat.events.ListData;
import firestream.chat.filter.Filter;
import firestream.chat.firebase.rx.DisposableMap;
import firestream.chat.interfaces.IAbstractChat;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import firestream.chat.firebase.service.Path;
import firestream.chat.message.Sendable;

import firestream.chat.types.SendableType;

/**
 * This class handles common elements of a conversation bit it 1-to-1 or group.
 * Mainly sending and receiving messages.
 */
public abstract class AbstractChat implements Consumer<Throwable>, IAbstractChat {

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
    protected ArrayList<Sendable> sendables = new ArrayList<>();

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
    protected Observable<Event<Sendable>> messagesOn() {
        return messagesOn(null);
    }

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    protected Observable<Event<Sendable>> messagesOn(Date newerThan) {
        return Fire.internal().getFirebaseService().core.messagesOn(messagesPath(), newerThan, Fire.internal().getConfig().messageHistoryLimit).doOnNext(event -> {
            Sendable sendable = event.get();
            Sendable previous = getSendable(sendable.getId());
            if (event.typeIs(EventType.Added)) {
                sendables.add(sendable);
            }
            if (previous != null) {
                if (event.typeIs(EventType.Modified)) {
                    sendable.copyTo(previous);
                }
                if (event.typeIs(EventType.Removed)) {
                    sendables.remove(previous);
                }
            }
            getSendableEvents().getSendables().onNext(event);
        }).doOnError(throwable -> {
            events.publishThrowable().onNext(throwable);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Get a updateBatch of messages once
     * @param fromDate get messages from this date
     * @param toDate get messages until this date
     * @param limit limit the maximum number of messages
     * @return a events of message results
     */
    protected Single<List<Sendable>> loadMoreMessages(@Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit) {
        return Fire.internal().getFirebaseService().core
                .loadMoreMessages(messagesPath(), fromDate, toDate, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Sendable>> loadMoreMessages(Date fromDate, Date toDate) {
        return loadMoreMessages(fromDate, toDate, null);
    }

    public Single<List<Sendable>> loadMoreMessagesFrom(Date fromDate, Integer limit) {
        return loadMoreMessages(fromDate, null, limit);
    }

    public Single<List<Sendable>> loadMoreMessagesTo(Date toDate, Integer limit) {
        return loadMoreMessages(null, toDate, limit);
    }

    public Single<List<Sendable>> loadMoreMessagesBefore(final Date toDate, Integer limit) {
        return Single.defer(() -> {
            Date before = toDate == null ? null : new Date(toDate.getTime() - 1);
            return loadMoreMessagesTo(before, limit);
        });
    }

    /**
     * This method gets the date of the last delivery receipt that we sent - i.e. the
     * last message WE received.
     * @return single date
     */
    protected Single<Date> dateOfLastDeliveryReceipt() {
        return Single.defer(() -> {
            if (!Fire.internal().getConfig().startListeningFromLastSentMessageDate) {
                return Single.just(Fire.internal().getConfig().listenToMessagesWithTimeAgo.getDate());
            } else {
                return Fire.internal().getFirebaseService().core
                        .dateOfLastSentMessage(messagesPath())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        });
    }

                /**
     * Listen for changes in the value of a list reference
     * @param path to listen to
     * @return events of list events
     */
    protected Observable<Event<ListData>> listChangeOn(Path path) {
        return Fire.internal().getFirebaseService().core
                .listChangeOn(path)
                .subscribeOn(Schedulers.io())
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
        return Fire.internal().getFirebaseService().core
                .send(messagesPath, sendable, newId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Delete a sendable from our queue
     * @param messagesPath
     * @return completion
     */
    protected Completable deleteSendable (Path messagesPath) {
        return Fire.internal().getFirebaseService().core
                .deleteSendable(messagesPath)
                .subscribeOn(Schedulers.io())
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
    protected Completable removeUsers(Path path, List<? extends User> users) {
        return Fire.internal().getFirebaseService().core
                .removeUsers(path, users)
                .subscribeOn(Schedulers.io())
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
    public Completable addUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Fire.internal().getFirebaseService().core
                .addUsers(path, dataProvider, users)
                .subscribeOn(Schedulers.io())
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
    public Completable updateUsers(Path path, User.DataProvider dataProvider, List<? extends User> users) {
        return Fire.internal().getFirebaseService().core
                .updateUsers(path, dataProvider, users)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void connect() throws Exception {
        dm.add(dateOfLastDeliveryReceipt()
                .flatMapObservable(this::messagesOn)
                .subscribeOn(Schedulers.io())
                .subscribe(this::passMessageResultToStream, this));
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
            events.getTypingStates().onNext(event.to(sendable.toTypingState()));
        }
        if (sendable.isType(SendableType.invitation())) {
            events.getInvitations().onNext(event.to(sendable.toInvitation()));
        }
        if (sendable.isType(SendableType.presence())) {
            events.getPresences().onNext(event.to(sendable.toPresence()));
        }
    }

    @Override
    public ArrayList<Sendable> getSendables() {
        return sendables;
    }

    @Override
    public ArrayList<Sendable> getSendables(SendableType type) {
        return Lists.newArrayList(Collections2.filter(sendables, input -> input != null && input.isType(type)));
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
        if (Fire.internal().getConfig().debugEnabled) {
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

}
