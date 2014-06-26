package com.braunster.chatsdk.interfaces;

/**
 * Created by itzik on 6/9/2014.
 */
public interface CompletionListenerWithData<E> {
    public void onDone(E e);
    public void onDoneWithError();
}


