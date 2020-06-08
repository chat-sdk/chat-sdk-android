package firestream.chat.interfaces;

import java.util.Date;
import java.util.List;

import firestream.chat.chat.Events;
import firestream.chat.message.Sendable;
import firestream.chat.types.SendableType;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.guru.common.DisposableMap;

public interface IAbstractChat extends Consumer<Throwable>, CompletableObserver {

    /**
     * Connect to the chat
     * @throws Exception error if we are not connected
     */
    void connect() throws Exception;

    /**
     * Disconnect from a chat. This does not affect our membership but we will
     * no longer receive any updates unless we log out / log in again
     */
    void disconnect();

    /**
     * When we leave / disconnect from a chat or when we log out, any disposables
     * will be disposed of automatically
     * @param disposable to manage
     */
    void manage(Disposable disposable);

    /**
     * Get the managed disposable map. This map will be disposed of when we leave / disconnect
     * from the chat or when we log out. Use this to store any disposables that you want to be
     * disposed of then. This type slightly more flexible than the manage method because it allows
     * you to store and retrieve disposables from an ID.
     * @return a pointer to the managed disposable map
     */
    DisposableMap getDisposableMap();

    /**
     * Get a list of all sendables received
     * @return a list of sendables
     */
    List<Sendable> getSendables();

    /**
     * Get a list of sendables given a class
     * @param clazz of sendable
     * @return list of sendables
     */
    <T extends Sendable> List<T> getSendables(Class<T> clazz);

    /**
     * Get a list of sendables filtered by type
     * @param type of sendable
     * @return a filtered list of sendables
     */
    List<Sendable> getSendables(SendableType type);

    /**
     * Get a sendable for a particular ID
     * @param id of sendable
     * @return sendable or null
     */
    Sendable getSendable(String id);

    /**
     * Get access to the events object which provides access to observables for sendable events
     * @return events holder
     */
    Events getSendableEvents();

    /**
     * Load a batch of historic messages
     *
     * @param fromDate load messages AFTER this date
     * @param toDate load message TO AND INCLUDING this date
     * @return a stream of messages
     */
    Single<List<Sendable>> loadMoreMessages(Date fromDate, Date toDate, boolean desc);

    /**
     * Load a batch of historic messages
     *
     * @param fromDate load messages AFTER this date
     * @param limit the number of messages returned
     * @return a stream of messages
     */
    Single<List<Sendable>> loadMoreMessagesFrom(Date fromDate, Integer limit, boolean desc);

    /**
     * Load a batch of historic messages
     *
     * @param toDate load message TO AND INCLUDING this date
     * @param limit the number of messages returned
     * @return a stream of messages
     */
    Single<List<Sendable>> loadMoreMessagesTo(Date toDate, Integer limit, boolean desc);

    /**
     * Load a batch of historic messages
     *
     * @param toDate load message TO  this date
     * @param limit the number of messages returned
     * @return a stream of messages
     */
    Single<List<Sendable>> loadMoreMessagesBefore(Date toDate, Integer limit, boolean desc);


}
