package firestream.chat.interfaces;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import firestream.chat.chat.Events;
import firestream.chat.firebase.rx.DisposableMap;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.SendableType;
import firestream.chat.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public interface IAbstractChat {

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
     * disposed of then. This is slightly more flexible than the manage method because it allows
     * you to store and retrieve disposables from an ID.
     * @return a pointer to the managed disposable map
     */
    DisposableMap getDisposableMap();

    /**
     * Get a list of all sendables received
     * @return a list of sendables
     */
    ArrayList<Sendable> getSendables();

    /**
     * Get a list of sendables filtered by type
     * @param type of sendable
     * @return a filtered list of sendables
     */
    ArrayList<Sendable> getSendables(SendableType type);

    /**
     * Get access to the events object which provides access to observables for sendable events
     * @return events holder
     */
    Events getSendableEvents();

}
