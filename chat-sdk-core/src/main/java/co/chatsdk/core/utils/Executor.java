package co.chatsdk.core.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by benjaminsmiley-andrews on 10/05/2017.
 */

// TODO: What does this actually do?
@Deprecated
public class Executor {
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 20;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    private static int MAX_THREADS = 15;

    private ThreadPoolExecutor threadPool;

    private static Executor instance;

    public static Executor getInstance() {
        if (instance == null)
            instance = new Executor();
        return instance;
    }

    private Executor(){

        if (NUMBER_OF_CORES <= 0)
            NUMBER_OF_CORES = 2;

        // Creates a thread pool manager
        threadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                workQueue);
    }

    public void execute(Runnable runnable){
        threadPool.execute(runnable);
    }

    private void restart(){
        threadPool.shutdownNow();
        instance = new Executor();
    }
}
