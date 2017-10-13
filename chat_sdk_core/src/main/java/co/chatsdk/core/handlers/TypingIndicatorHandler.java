package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;
import io.reactivex.Completable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface TypingIndicatorHandler {

    enum State {
        /**
         * User is actively participating in the chat session.
         */
        active,
        /**
         * User is composing a message.
         */
        composing,
        /**
         * User had been composing but now has stopped.
         */
        paused,
        /**
         * User has not been actively participating in the chat session.
         */
        inactive,
        /**
         * User has effectively ended their participation in the chat session.
         */
        gone
    }

    void typingOn (Thread thread);
    void typingOff (Thread thread);
    Completable setChatState (State state, Thread thread);
//
//    -(RXPromise *) setChatState: (bChatState) state forThread: (id<PThread>) thread;
}
