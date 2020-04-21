package sdk.guru.common;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import sdk.guru.common.RX;
import io.reactivex.schedulers.Schedulers;
import sdk.guru.common.RX;

public class RX {

    protected static ExecutorService firebaseExecutorService;

    public static Scheduler single() {
        return Schedulers.single();
    }

    public static Scheduler io() {
        return Schedulers.io();
    }

    public static Scheduler computation() {
        return Schedulers.computation();
    }

    public static Scheduler main() {
        return AndroidSchedulers.mainThread();
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
     * For longer listeners
     * @return scheduler
     */
    public static Scheduler pool() {
        if (firebaseExecutorService == null) {
            firebaseExecutorService = Executors.newFixedThreadPool(10);
        }
        return Schedulers.from(firebaseExecutorService);
    }

    public static Scheduler newThread() {
        return Schedulers.newThread();
    }

    public static void onMain(Runnable runnable) {
        main().scheduleDirect(runnable);
    }

    public static void onBackground(Runnable runnable) {
        computation().scheduleDirect(runnable);
    }

    public static void run(Runnable onBackground, Runnable onMain) {
        onBackground(() -> {
            onBackground.run();
            onMain(onMain);
        });
    }
}
