package com.braunster.chatsdk.interfaces;

/**
 * Created by braunster on 30/06/14.
 */

public interface AppBatchedEvents {

    public boolean onUsersBatchDetailsChange();

    public boolean onMessagesBatchReceived();

    public boolean onThreadsBatchIsAdded();
    public boolean onThreadDetailsChanged();
    public boolean onUsersBatchAddedToThread();

}
