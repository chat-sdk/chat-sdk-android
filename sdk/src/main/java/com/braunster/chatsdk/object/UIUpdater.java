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