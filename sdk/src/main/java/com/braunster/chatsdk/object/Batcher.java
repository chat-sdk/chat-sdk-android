/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.object;

import android.os.Handler;

import java.lang.ref.WeakReference;
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
    private 
    BatchedAction<T> batchedAction;
    private Timer timer = new Timer();
    private WeakReference<Handler> handler;

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
        
        if (handler != null)
            this.handler = new WeakReference<Handler>(handler);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (pulled && !toHold())
                {
                    trigger();
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, this.interval, this.interval);

        setBatchedAction(action);
    }

    public synchronized boolean add(T t){
        if (t!=null)
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

    private synchronized void pull(){
        pulledTime = System.currentTimeMillis();
        pulled = true;
    }

    private Runnable triggerRunnable = new Runnable() {
        @Override
        public void run() {
            pulled = false;

            if (batchedAction != null)
                batchedAction.triggered(new CopyOnWriteArrayList<T>(stash));

            stash.clear();

            pulledTime = 0;
        }
    };

    private synchronized void trigger(){
        if (handler != null && handler.get() != null)
        {
            handler.get().removeCallbacks(triggerRunnable);
            handler.get().post(triggerRunnable);
        }
        else triggerRunnable.run();
    }

    public boolean isPulled() {
        return pulled;
    }

    private synchronized boolean toHold(){
        return  pulledTime == 0 || System.currentTimeMillis() - pulledTime < interval;
    }

    public void setBatchedAction(BatchedAction<T> batchedAction) {
        this.batchedAction = batchedAction;
    }

    public void kill(){
        if (timer != null)
            timer.cancel(); 
        
        if (timerTask != null)
            timerTask.cancel();
    }
    
    public interface BatchedAction<T>{
        public void triggered(List<T> list);
    }
}
