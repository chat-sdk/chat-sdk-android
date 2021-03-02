package sdk.chat.core.utils;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor implements LifecycleObserver {

    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private boolean enabled = false;
    private boolean inBackground = true;

    protected Set<StartListener> startListeners = new HashSet<>();
    protected Set<StopListener> stopListeners = new HashSet<>();

    public interface Listener extends StartListener, StopListener {
    }

    public interface StartListener {
        void didStart();
    }

    public interface StopListener {
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
    public void onAppForeground() {
        inBackground = false;
        for (StartListener l : startListeners) {
            l.didStart();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground() {
        inBackground = true;
        for (StopListener l : stopListeners) {
            l.didStop();
        }
    }

    public void addListener (StartListener listener) {
        startListeners.add(listener);
    }

    public void addListener (StopListener listener) {
        stopListeners.add(listener);
    }

    public void addListener (Listener listener) {
        startListeners.add(listener);
        stopListeners.add(listener);
    }

    public void removeListener (Listener listener) {
        startListeners.remove(listener);
        stopListeners.remove(listener);
    }

    public void removeListener (StartListener listener) {
        startListeners.remove(listener);
    }

    public void removeListener (StopListener listener) {
        stopListeners.remove(listener);
    }

    public boolean inBackground () {
        return inBackground;
    }

    public void stop() {
        startListeners.clear();
        stopListeners.clear();
    }
}
