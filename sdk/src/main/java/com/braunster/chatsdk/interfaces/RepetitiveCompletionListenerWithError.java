package com.braunster.chatsdk.interfaces;

/**
 * Created by braunster on 23/06/14.
 */
public interface RepetitiveCompletionListenerWithError<ITEM, ERROR> {
    public boolean onItem(ITEM item);
    public void onDone();
    public void onItemError(ITEM item, ERROR error);
}
