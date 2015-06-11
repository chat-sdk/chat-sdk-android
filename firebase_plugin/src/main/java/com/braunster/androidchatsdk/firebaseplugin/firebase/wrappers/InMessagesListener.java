/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseEventsManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.events.AppEventListener;
import com.firebase.client.DataSnapshot;

import org.jdeferred.Deferred;

import timber.log.Timber;

public class InMessagesListener extends FirebaseGeneralEvent {

    private static final String TAG = InMessagesListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.IncomingMessagesListener;
    private Handler handler;

    private boolean isNew = false;
    private long creationTime;
    private String threadEntityId;

    private Deferred<BThread, Void, Void> deferred;
    
    public InMessagesListener(Handler handler, String threadEntiyId, Deferred<BThread, Void, Void>  deferred){
        super(ChildEvent);
        this.deferred = deferred;
        this.threadEntityId = threadEntiyId;
        this.handler = handler;
        creationTime = System.currentTimeMillis();
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, final String s) {
        if (DEBUG) Timber.v("Message has arrived, Alive: %s", isAlive());
        if (isAlive())
            FirebaseEventsManager.Executor.getInstance().execute(new Runnable() {
                @Override
                public void run() {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    
                    // Rejecting no value messages.
                    if (dataSnapshot.getValue() == null)
                    {
                        if (deferred != null  &&  deferred.isPending())
                            deferred.reject(null);
                        return;
                    }
                    
                    BMessageWrapper wrapper = BMessageWrapper.initWithSnapshot(dataSnapshot);

                    wrapper.setDelivered(BMessage.Delivered.Yes);
                    
                    // Checking for null sender and that the sender isn't the current user.
                    // This will make sure we wont notify user for his own messages.
                    BUser sender = wrapper.model.getSender();

                    if (!sender.isMe())
                    {
                        // Set the message as new if was told from creator,
                        // Or if the date of the message is later then the creation of this object.
                        if (isNew)
                            wrapper.model.setIsRead(false);
                        else if (creationTime < wrapper.model.getDate().getTime())
                            wrapper.model.setIsRead(false);
                    }

                    
                    BThread thread = DaoCore.fetchOrCreateEntityWithEntityID(BThread.class, threadEntityId);

                    Timber.d("New Message, MyMessage: %s, ThreadType: %s, ", sender.isMe(), thread.getType());

                    // If the user is blocked and this is a private thread then we just return
                    // don't let the message get through
                    if (!sender.isMe() && thread.getType().equals(BThread.Type.OneToOne))
                    {
                        if (sender.isBlocked())
                        {
                            if (deferred != null &&  deferred.isPending())
                                deferred.resolve(thread);

                            // Deleting the message
                            DaoCore.deleteEntity(wrapper.model);
                            return;
                        }
                    }

                    // Checking to see if this thread was deleted.
                    if (thread.isDeleted())
                    {
                        if (DEBUG) Timber.v("Thread is Deleted");

                        // Making sure we are now listening to all events.
                        BThreadWrapper threadWrapper = new  BThreadWrapper(thread);
                        threadWrapper.on();
                        threadWrapper.usersOn();
                        threadWrapper.messagesOn();
                        threadWrapper.recoverThread();
                    }


                    // Update the thread
                    DaoCore.updateEntity(thread);

                    // Update the message.
                    wrapper.model.setThread(thread);
                    DaoCore.updateEntity(wrapper.model);

                    if (deferred != null &&  deferred.isPending())
                        deferred.resolve(thread);
                    
                    Message message = new Message();
                    message.what = AppEventListener.MESSAGE_RECEIVED;
                    message.obj = wrapper.model;
                    handler.sendMessageAtFrontOfQueue(message);
                }
            });
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
