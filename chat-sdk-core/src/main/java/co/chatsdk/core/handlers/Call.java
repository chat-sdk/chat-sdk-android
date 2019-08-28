package co.chatsdk.core.handlers;

import co.chatsdk.core.enums.CallState;

/**
 * Created by Pepe Becker on 27/08/2019.
 */

public interface Call {

    void answer();
    void hangup();
    void pauseVideo();
    void resumeVideo();
    void addCallListener(CallListener callListener);

    String getCallId();
    String getRemoteUserId();
    CallState getState();
    boolean isOutgoing();

}
