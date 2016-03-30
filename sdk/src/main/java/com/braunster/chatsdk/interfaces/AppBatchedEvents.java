/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

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
