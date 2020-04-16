package sdk.guru.common;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import sdk.guru.common.RX;

public class RX {

    public static Scheduler single() {
        return Schedulers.single();
    }

    public static Scheduler io() {
        return Schedulers.io();
    }

    public static Scheduler computation() {
        return Schedulers.computation();
    }

    /**
     * Quick tasks that will finish fast
     * @return scheduler
     */
    public static Scheduler quick() {
        return Schedulers.computation();
    }

    /**
     * Database operations
     * @return scheduler
     */
    public static Scheduler db() {
        return Schedulers.computation();
    }

    /**
     * Firebase requests
     * @return scheduler
     */
    public static Scheduler firebase() {
        return Schedulers.io();
    }

    public static Scheduler newThread() {
        return Schedulers.newThread();
    }

    public static void onMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
