package com.braunster.chatsdk.network.events;

import android.os.Handler;

import com.braunster.chatsdk.object.Batcher;

import static com.braunster.chatsdk.network.events.Event.Type.AppEvent;

public class BatchedEvent extends Event{

    private Batcher<String> threadBatch, threadAddedBatcher, userDetailsBatcher, MessageBatcher, followerBatcher;
    private Handler handler;

    public BatchedEvent(String tag, String entityId, Type type) {
        super(tag, entityId, type);
    }

    public BatchedEvent(String tag, String entityId, Type type, Handler handler) {
        super(tag, entityId, type);
        this.handler = handler;
    }

    private void initBatcher(Type type, Batcher.BatchedAction<String> action, long interval ){
        if (interval == -1)
            interval = Batcher.DEF_INTERVAL;

        switch (type){

            case AppEvent:
                threadBatch = new Batcher(action, interval, handler);
                threadAddedBatcher = new Batcher(action, interval, handler);
                userDetailsBatcher = new Batcher(action, interval, handler);
                MessageBatcher = new Batcher(action, interval, handler);
                followerBatcher = new Batcher(action, interval, handler);
                break;

            case MessageEvent:
                MessageBatcher = new Batcher(action, interval, handler);
                break;

            case ThreadAddedEvent:
                threadAddedBatcher = new Batcher(action, interval, handler);
                break;

            case ThreadEvent:
                threadBatch = new Batcher(action, interval, handler);
                break;

            case UserDetailsEvent:
                userDetailsBatcher = new Batcher(action, interval, handler);
                break;

            case FollwerEvent:
                followerBatcher = new Batcher(action, interval, handler);
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
        if (this.type != AppEvent &&  type != this.type)
            return;

        switch (type){

            case AppEvent:
                threadBatch.add(entityID);
                threadAddedBatcher.add(entityID);
                userDetailsBatcher.add(entityID);
                MessageBatcher.add(entityID);
                followerBatcher.add(entityID);
                break;

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
}
