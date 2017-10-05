package co.chatsdk.ui.utils;

import java.util.Timer;
import java.util.TimerTask;

import co.chatsdk.core.NM;

/**
 * Created by ben on 9/27/17.
 */

public class AppBackgroundMonitor {

    public static final AppBackgroundMonitor instance = new AppBackgroundMonitor();

    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    private boolean wasInBackground = false;
    private long MAX_ACTIVITY_TRANSITION_TIME = 5000;

    public static AppBackgroundMonitor shared () {
        return instance;
    }

    public void startActivityTransitionTimer() {
        if(!NM.auth().userAuthenticated()) {
            return;
        }
        this.activityTransitionTimer = new Timer();
        this.activityTransitionTimerTask = new TimerTask() {
            public void run() {
                wasInBackground = true;
                NM.core().goOffline();
            }
        };

        this.activityTransitionTimer.schedule(activityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME);
    }

    public void stopActivityTransitionTimer() {
        if (this.activityTransitionTimerTask != null) {
            this.activityTransitionTimerTask.cancel();
        }

        if (this.activityTransitionTimer != null) {
            this.activityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }

    public boolean wasInBackground () {
        return wasInBackground;
    }
}
