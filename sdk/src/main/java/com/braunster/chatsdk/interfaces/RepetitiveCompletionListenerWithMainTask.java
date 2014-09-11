package com.braunster.chatsdk.interfaces;

/**
 * Created by braunster on 23/06/14.
 *
 * I - is the item that you want to receive in the onItem callback.
 * MI - is the main item you want to receive in the onMainFinished callback.
 */

public interface RepetitiveCompletionListenerWithMainTask<MI, I> extends RepetitiveCompletionListener<I> {
    public boolean onMainFinised(MI mi, Object error);

    @Override
    boolean onItem(I item);

    @Override
    void onDone();

    @Override
    void onItemError(Object object);
}
