package co.chatsdk.ui.utils;

import java.util.Timer;
import java.util.TimerTask;

import co.chatsdk.core.session.NM;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor {

    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    private boolean wasInBackground = false;
    private long MaxActivityTransitionTime = 5000;
    private boolean enabled = false;

    public static AppBackgroundMonitor shared () {
        return instance;
    }

    public void startActivityTransitionTimer() {
        if(!enabled || !NM.auth().userAuthenticated()) {
            return;
        }
        activityTransitionTimer = new Timer();
        activityTransitionTimerTask = new TimerTask() {
            public void run() {
                wasInBackground = true;
                NM.core().goOffline();
            }
        };

        this.activityTransitionTimer.schedule(activityTransitionTimerTask,
                MaxActivityTransitionTime);
    }

    public void stopActivityTransitionTimer() {
        if (!enabled) {
            return;
        }

        if (activityTransitionTimerTask != null) {
            activityTransitionTimerTask.cancel();
        }

        if (activityTransitionTimer != null) {
            activityTransitionTimer.cancel();
        }

//        if(wasInBackground) {
            if(NM.auth().userAuthenticated()) {
                NM.core().goOnline();
            }
//        }

        wasInBackground = false;
    }

    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
        if(!enabled) {
            cancel();
        }
    }

    public void cancel () {
        wasInBackground = false;
        stopActivityTransitionTimer();
    }

    public boolean wasInBackground () {
        return wasInBackground;
    }
}
