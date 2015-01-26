package com.braunster.chatsdk.interfaces;

import com.braunster.chatsdk.object.BError;

/**
 * Created by braunster on 17/11/14.
 */
public interface SaveCompletedListener {
    public void onSaved(BError error, String url);
}
