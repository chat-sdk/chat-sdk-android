package com.braunster.chatsdk.interfaces;

/**
 * Created by braunster on 23/06/14.
 *
 * I - is the item that you want to receive in the onItem callback.
 * MI - is the main item you want to receive in the onMainFinished callback.
 */

public interface RepetitiveCompletionListenerWithMainTaskAndError<MI, I, ERROR> extends RepetitiveCompletionListenerWithError<I, ERROR> {
    public boolean onMainFinised(MI mi, ERROR error);

    @Override
    boolean onItem(I item);

    @Override
    void onDone();

    @Override
    void onItemError(I i, ERROR error);
}
