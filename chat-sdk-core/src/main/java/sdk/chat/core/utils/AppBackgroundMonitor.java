package sdk.chat.core.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import org.pmw.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import sdk.chat.core.session.ChatSDK;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor implements LifecycleObserver, DefaultLifecycleObserver {

//    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private boolean enabled = false;
    private boolean inBackground = true;

    protected Set<StartListener> startListeners = new HashSet<>();
    protected Set<StopListener> stopListeners = new HashSet<>();

    public void onStart(LifecycleOwner owner) { // app moved to foreground
        onAppForeground();
    }

    public void onStop(LifecycleOwner owner) { // app moved to background
        onAppBackground();
    }

    public void onResume(@NonNull LifecycleOwner owner) {
        Logger.info("");
    }

    public void onPause(@NonNull LifecycleOwner owner) {
        Logger.info("");
    }

    public interface Listener extends StartListener, StopListener {
    }

    public interface StartListener {
        void didStart();
    }

    public interface StopListener {
        void didStop();
    }

    @Deprecated
    /**
     * Use ChatSDK.backgroundMonitor()
     */
    public static AppBackgroundMonitor shared () {
        return ChatSDK.backgroundMonitor();
    }

    public void setEnabled(boolean enabled) {
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

    //    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForeground() {
        inBackground = false;
        for (StartListener l : startListeners) {
            l.didStart();
        }
    }

    //    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackground() {
        inBackground = true;
        for (StopListener l : stopListeners) {
            l.didStop();
        }
    }

    public void addListener(StartListener listener) {
        startListeners.add(listener);
    }

    public void addListener(StopListener listener) {
        stopListeners.add(listener);
    }

    public void addListener(Listener listener) {
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
