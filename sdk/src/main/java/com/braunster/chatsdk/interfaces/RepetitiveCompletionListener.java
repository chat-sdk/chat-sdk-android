package com.braunster.chatsdk.interfaces;

/**
 * Created by braunster on 23/06/14.
 */
public interface RepetitiveCompletionListener<I> {
    public boolean onItem(I item);
    public void onDone();
    public void onItemError(Object object);
}
