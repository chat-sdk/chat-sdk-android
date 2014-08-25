package com.braunster.chatsdk.object;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by braunster on 23/08/14.
 */
public class Batcher<T> {
    public static final long DEF_INTERVAL = 1500;

    private long pulledTime = 0, interval = DEF_INTERVAL;
    private boolean pulled = false;
    private TimerTask timerTask;
    private BatchedAction<T> batchedAction;
    private Timer timer = new Timer();
    private Handler handler;

    private List<T> stash = new ArrayList<T>();

    public Batcher(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (pulled && !toHold())
                    trigger();
            }
        };

        timer.scheduleAtFixedRate(timerTask, interval, interval);
    }

    public Batcher(BatchedAction<T> action){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (pulled && !toHold())
                    trigger();
            }
        };

        timer.scheduleAtFixedRate(timerTask, interval, interval);

        setBatchedAction(action);
    }

    public Batcher(BatchedAction<T> action, long interval, final Handler handler){
        this.interval = interval;
        this.handler = handler;

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (pulled && !toHold())
                {
                    if (handler != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                trigger();
                            }
                        });
                    else trigger();
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, this.interval, this.interval);

        setBatchedAction(action);
    }

    public boolean add(T t){
        stash.add(t);

        if (!pulled)
        {
            pull();
            return false;
        }

        if (!toHold())
        {
            trigger();
            return true;
        }

        return false;
    }

    private void pull(){
        pulledTime = System.currentTimeMillis();
        pulled = true;
    }

    private void trigger(){
        pulled = false;

        if (batchedAction != null)
            batchedAction.triggered(new CopyOnWriteArrayList<T>(stash));

        stash.clear();

        pulledTime = 0;
    }

    public boolean isPulled() {
        return pulled;
    }

    private boolean toHold(){
        return  pulledTime == 0 || System.currentTimeMillis() - pulledTime < interval;
    }

    public void setBatchedAction(BatchedAction<T> batchedAction) {
        this.batchedAction = batchedAction;
    }

    public interface BatchedAction<T>{
        public void triggered(List<T> list);
    }
}
