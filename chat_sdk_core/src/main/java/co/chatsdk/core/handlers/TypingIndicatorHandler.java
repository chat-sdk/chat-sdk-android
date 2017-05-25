package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.core.BThread;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface TypingIndicatorHandler {

    public void typingOn (BThread thread);
    public void typingOff (BThread thread);
//
//    -(RXPromise *) setChatState: (bChatState) state forThread: (id<PThread>) thread;
}
