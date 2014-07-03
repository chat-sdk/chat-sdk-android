package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.object.BError;

/**
 * Created by itzik on 6/9/2014.
 */
public interface CompletionListenerWithData<E> {
    public void onDone(E e);
    public void onDoneWithError(BError error);
}


