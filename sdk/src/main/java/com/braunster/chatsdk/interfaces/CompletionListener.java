package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.object.BError;

/**
 * Created by itzik on 6/8/2014.
 */
public interface CompletionListener {
    public void onDone();
    public void onDoneWithError(BError error);
}
