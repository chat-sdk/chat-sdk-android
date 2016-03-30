/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.object;

/**
 * Created by braunster on 14/08/14.
 */

public abstract class UIUpdater implements Runnable{

    private boolean killed = false;

    public void setKilled(boolean killed) {
        this.killed = killed;
    }

    public boolean isKilled() {
        return killed;
    }
}