

package com.braunster.chatsdk.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by braunster on 18/08/14.
 */
public class ChatSDKImageMessagesThreadPool {
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

    private static int MAX_POOL_SIZE = NUMBER_OF_CORES * 2;

    private PausableThreadPoolExecutor threadPool;

    private static ChatSDKImageMessagesThreadPool instance;

    public static ChatSDKImageMessagesThreadPool getInstance() {
        if (instance == null)
            instance = new ChatSDKImageMessagesThreadPool();
        return instance;
    }

    private ChatSDKImageMessagesThreadPool(){

        if (NUMBER_OF_CORES < 0)
        {
            NUMBER_OF_CORES = 2;

            Timber.i("Number of cores == 0");
        }

        if (NUMBER_OF_CORES > MAX_POOL_SIZE)
        {
            MAX_POOL_SIZE = NUMBER_OF_CORES * 2;

            Timber.i("Max thread pool size is smaller then the core, Core: %s, Max: %s", NUMBER_OF_CORES, MAX_POOL_SIZE);
        }

        // Creates a thread pool manager
        threadPool = new PausableThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                MAX_POOL_SIZE,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                workQueue);

    }

    public PausableThreadExecutor getThreadPool() {
        return threadPool;
    }

    /**
     * Execute a Runnable.
     * * * */
    public void execute(Runnable runnable){
        threadPool.execute(runnable);
    }

    /**
     * Execute a Runnable, The callback set will be called after the runnable was executed.
     * * * */
    public void execute(final Runnable runnable, final ExecutionFinishedListener listener){
        execute(new Runnable() {
            @Override
            public void run() {
                runnable.run();

                if (listener != null)
                    listener.done();
            }
        });
    }

    public interface ExecutionFinishedListener{
        public void done();
    }


}
