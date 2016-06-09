/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.object;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by braunster on 18/08/14.
 */
public class ChatSDKThreadPool {
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 3;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    private ThreadPoolExecutor threadPool;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private static ChatSDKThreadPool instance;

    public static ChatSDKThreadPool getInstance() {
        if (instance == null)
            instance = new ChatSDKThreadPool();
        return instance;
    }

    private ChatSDKThreadPool(){

        if (NUMBER_OF_CORES <= 0)
            NUMBER_OF_CORES = 2;

        // Creates a thread pool manager
        threadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                workQueue);

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(NUMBER_OF_CORES);

    }

    public void execute(Runnable runnable){
        threadPool.execute(runnable);
    }

    public void scheduleExecute(Runnable runnable, long delay){
        scheduledThreadPoolExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
    }

    public boolean removeSchedule(Runnable runnable){
        return scheduledThreadPoolExecutor.remove(runnable);
    }
}
