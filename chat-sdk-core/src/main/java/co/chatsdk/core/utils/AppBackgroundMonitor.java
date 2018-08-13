package co.chatsdk.core.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor implements LifecycleObserver {

    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private boolean enabled = false;
    private boolean inBackground = true;

    public static AppBackgroundMonitor shared () {
        return instance;
    }

    public void setEnabled (boolean enabled) {
        if (enabled == this.enabled) {
            return;
        }
        this.enabled = enabled;
        if(enabled) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        }
        else {
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForeground () {
        inBackground = false;
        if(ChatSDK.auth().userAuthenticated() && ChatSDK.config().disconnectFromFirebaseWhenInBackground) {
            ChatSDK.core().goOnline();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground () {
        inBackground = true;
        if (ChatSDK.config().disconnectFromFirebaseWhenInBackground) {
            ChatSDK.core().goOffline();
        }
    }

    public boolean inBackground () {
        return inBackground;
    }
}
