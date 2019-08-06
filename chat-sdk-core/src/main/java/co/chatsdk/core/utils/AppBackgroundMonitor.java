package co.chatsdk.core.utils;

import java.util.ArrayList;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import co.chatsdk.core.session.ChatSDK;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor implements LifecycleObserver {

    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private boolean enabled = false;
    private boolean inBackground = true;

    protected ArrayList<Listener> listeners = new ArrayList<>();

    public interface Listener {
        void didStart();
        void didStop();
    }

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
        if(ChatSDK.auth().isAuthenticated() && ChatSDK.config().disconnectFromFirebaseWhenInBackground) {
            ChatSDK.core().goOnline();
        }
        for (Listener l : listeners) {
            l.didStart();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground () {
        inBackground = true;
        if (ChatSDK.config().disconnectFromFirebaseWhenInBackground) {
            ChatSDK.core().goOffline();
        }
        for (Listener l : listeners) {
            l.didStop();
        }
    }

    public void addListener (Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener (Listener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public boolean inBackground () {
        return inBackground;
    }
}
