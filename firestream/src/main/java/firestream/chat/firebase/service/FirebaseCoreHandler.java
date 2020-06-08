package firestream.chat.firebase.service;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

import firestream.chat.chat.User;
import firestream.chat.events.ListData;
import firestream.chat.message.Sendable;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import sdk.guru.common.Event;
import sdk.guru.common.Optional;

public abstract class FirebaseCoreHandler {

    /**
     * Listen for changes in the value of a list reference
     *
     * @param path to listen to
     * @return events of list events
     */
    public abstract Observable<Event<ListData>> listChangeOn(Path path);

    /**
     * Delete a sendable from our queue
     *
     * @param messagesPath
     * @return completion
     */
    public abstract Completable deleteSendable(Path messagesPath);

    /**
     * Send a message to a messages ref
     *
     * @param messagesPath Firestore reference for message collection
     * @param sendable item to be sent
     * @param newId get the id of the new message before it's sent
     * @return completion
     */
    public abstract Completable send(Path messagesPath, Sendable sendable, Consumer<String> newId);

    /**
     * Add users to a reference
     *
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users        to add
     * @return completion
     */
    public abstract Completable addUsers(Path path, User.DataProvider dataProvider, List<? extends User> users);

    /**
     * Remove users from a reference
     *
     * @param path  for users
     * @param users to remove
     * @return completion
     */
    public abstract Completable removeUsers(Path path, List<? extends User> users);

    /**
     * Update users for a reference
     * @param path for users
     * @param dataProvider a callback to extract the data to add from the user
     *                     this allows us to use one method to write to multiple different places
     * @param users to update
     * @return completion
     */
    public abstract Completable updateUsers(Path path, User.DataProvider dataProvider, List<? extends User> users);

    /**
     * Get a updateBatch of messages once
     * @param messagesPath
     * @param fromDate get messages from this date
     * @param toDate get messages until this date
     * @param limit limit the maximum number of messages
     * @return a events of message results
     */
    public abstract Single<List<Sendable>> loadMoreMessages(Path messagesPath, @Nullable Date fromDate, @Nullable Date toDate, @Nullable Integer limit);

    /**
     * This method gets the date of the last delivery receipt that we sent - i.e. the
     * last message WE received.
     * @param messagesPath
     * @return single date
     */
    public abstract Single<Optional<Sendable>> lastMessage(Path messagesPath);

    /**
     * Start listening to the current message reference and pass the messages to the events
     * @param messagesPath
     * @param newerThan only listen for messages after this date
     * @return a events of message results
     */
    public abstract Observable<Event<Sendable>> messagesOn(Path messagesPath, Date newerThan);

    /**
     * Return a Firebase timestamp object
     * @return appropriate server timestamp object
     */
    public abstract Object timestamp();

    public abstract Completable mute(Path path, Map<String, Object> data);
    public abstract Completable unmute(Path path);

}