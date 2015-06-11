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

public class BatchedEvent extends Event{

    private Batcher<String> appBatch, onlineBatcher, friendsBatched, blockedBatcher,  threadBatch, threadAddedBatcher, userDetailsBatcher, MessageBatcher, followerBatcher;
    private WeakReference<Handler> handler;

    public BatchedEvent(String tag, String entityId) {
        super(tag, entityId);
    }

    public BatchedEvent(String tag, String entityId, Handler handler) {
        super(tag, entityId);
        
        if (handler != null)
            this.handler = new WeakReference<Handler>(handler);
    }

    private void initBatcher(Type type, Batcher.BatchedAction<String> action, long interval ){
        

        if (interval == -1)
            interval = Batcher.DEF_INTERVAL;

        switch (type){

            case AppEvent:
                appBatch = getBatcher(action, interval);
                break;

            case MessageEvent:
                MessageBatcher = getBatcher(action, interval);
                break;

            case ThreadAddedEvent:
                threadAddedBatcher = getBatcher(action, interval);
                break;

            case ThreadEvent:
                threadBatch = getBatcher(action, interval);
                break;

            case UserDetailsEvent:
                userDetailsBatcher = getBatcher(action, interval);
                break;

            case FollwerEvent:
                followerBatcher = getBatcher(action, interval);
                break;

            case OnlineChangeEvent:
                onlineBatcher = getBatcher(action, interval);
                break;

            case FriendsChangeEvent:
                friendsBatched = getBatcher(action, interval);
                break;

            case BlockedChangedEvent:
                blockedBatcher = getBatcher(action, interval);
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

        if (appBatch != null)
            appBatch.add(entityID);

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

            case OnlineChangeEvent:
                if (onlineBatcher ==null)
                    return;
                onlineBatcher.add(entityID);
                break;


            case FriendsChangeEvent:
                if (friendsBatched ==null)
                    return;
                friendsBatched.add(entityID);
                break;


            case BlockedChangedEvent:
                if (blockedBatcher ==null)
                    return;
                blockedBatcher.add(entityID);
                break;


        }
    }

    @Override
    public void kill() {
        super.kill();

        killBatch(appBatch);

        killBatch(MessageBatcher);

        killBatch(threadAddedBatcher);
        killBatch(threadBatch);
        killBatch(userDetailsBatcher);
        killBatch(followerBatcher);
        killBatch(onlineBatcher);
        killBatch(friendsBatched);
        killBatch(blockedBatcher);
    }

    private void killBatch(Batcher batchedEvent){
        if (batchedEvent != null)
            batchedEvent.kill();
    }
    private Batcher<String> getBatcher(Batcher.BatchedAction<String> action, long interval){
        return new Batcher<>(action, interval, handler != null ? handler.get() : null);
    }
}
