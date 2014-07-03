package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.object.BError;

/**
 * Created by itzik on 6/9/2014.
 * DATA - the data that we want to get when the task is done.
 * ERROR - is the object that we want to get as error.
 */
public interface CompletionListenerWithDataAndError<DATA, ERROR> {
    public void onDone(DATA data);
    public void onDoneWithError(DATA data, ERROR error);

}


