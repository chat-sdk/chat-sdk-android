package com.braunster.chatsdk.network.listeners;

import com.braunster.chatsdk.object.BError;

/**
 * Created by braunster on 11/07/14.
 */
public interface AuthListener{
    public void onCheckDone(boolean isAuthenticated);
    public void onLoginDone();
    public void onLoginFailed(BError error);
}
