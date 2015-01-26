package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.object.BError;

/**
 * Created by braunster on 23/06/14.
 */
public interface RepetitiveCompletionListener<I> {
    public boolean onItem(I item);
    public void onDone();
    public void onItemError(BError object);
}
