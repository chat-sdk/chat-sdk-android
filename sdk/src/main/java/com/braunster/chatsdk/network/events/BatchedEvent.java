/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network.events;

import android.os.Handler;

import com.braunster.chatsdk.object.Batcher;

import java.lang.ref.WeakReference;

import static com.braunster.chatsdk.network.events.Event.Type.AppEvent;

public class BatchedEvent extends Event{

    private Batcher<String> appBatch, threadBatch, threadAddedBatcher, userDetailsBatcher, MessageBatcher, followerBatcher;
    private WeakReference<Handler> handler;

    public BatchedEvent(String tag, String entityId, Type type) {
        super(tag, entityId, type);
    }

    public BatchedEvent(String tag, String entityId, Type type, Handler handler) {
        super(tag, entityId, type);
        
        if (handler != null)
            this.handler = new WeakReference<Handler>(handler);
    }

    private void initBatcher(Type type, Batcher.BatchedAction<String> action, long interval ){
        

        if (interval == -1)
            interval = Batcher.DEF_INTERVAL;

        switch (type){

            case AppEvent:
                appBatch = new Batcher<String>(action, interval, handler != null ? handler.get() : null);
                break;

            case MessageEvent:
                MessageBatcher = new Batcher(action, interval, handler != null ? handler.get() : null);
                break;

            case ThreadAddedEvent:
                threadAddedBatcher = new Batcher(action, interval, handler != null ? handler.get() : null);
                break;

            case ThreadEvent:
                threadBatch = new Batcher(action, interval, handler != null ? handler.get() : null);
                break;

            case UserDetailsEvent:
                userDetailsBatcher = new Batcher(action, interval, handler != null ? handler.get() : null);
                break;

            case FollwerEvent:
                followerBatcher = new Batcher(action, interval, handler != null ? handler.get() : null);
                break;
        }
    }

    public void setBatchedAction(Type type, Batcher.BatchedAction<String> batchedAction) {
        initBatcher(type, batchedAction, -1);
    }

    public void setBatchedAction(Type type, long interval, Batcher.BatchedAction<String> batchedAction) {
        initBatcher(type, batchedAction, interval);
    }

    public void add(Type type){
        add(type, null);
    }

    public void add(Type type, String entityID){
        if (this.type == AppEvent)
        {
            if (appBatch!=null)
                appBatch.add(entityID);
            return;
        }
        else
            if (type != this.type)
                return;

        switch (type){
            case MessageEvent:
                if (MessageBatcher==null)
                    return;
                MessageBatcher.add(entityID);
                break;

            case ThreadAddedEvent:
                if (threadAddedBatcher==null)
                    return;
                threadAddedBatcher.add(entityID);
                break;

            case ThreadEvent:
                if (threadBatch==null)
                    return;
                threadBatch.add(entityID);
                break;

            case UserDetailsEvent:
                if (userDetailsBatcher==null)
                    return;
                userDetailsBatcher.add(entityID);
                break;

            case FollwerEvent:
                if (followerBatcher==null)
                    return;
                followerBatcher.add(entityID);
                break;
        }
    }

    @Override
    public void kill() {
        super.kill();

        if (this.type == AppEvent)
        {
            if (appBatch!=null)
                appBatch.kill();
            return;
        }

        switch (type){
            case MessageEvent:
                if (MessageBatcher==null)
                    return;
                MessageBatcher.kill();
                break;

            case ThreadAddedEvent:
                if (threadAddedBatcher==null)
                    return;
                threadAddedBatcher.kill();
                break;

            case ThreadEvent:
                if (threadBatch==null)
                    return;
                threadBatch.kill();
                break;

            case UserDetailsEvent:
                if (userDetailsBatcher==null)
                    return;
                userDetailsBatcher.kill();
                break;

            case FollwerEvent:
                if (followerBatcher==null)
                    return;
                followerBatcher.kill();
                break;
        }
    }
}
