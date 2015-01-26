package com.braunster.androidchatsdk.firebaseplugin.firebase.listeners;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.braunster.androidchatsdk.firebaseplugin.firebase.BFirebaseInterface;
import com.braunster.androidchatsdk.firebaseplugin.firebase.EventManager;
import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;
import com.firebase.client.DataSnapshot;


/**
 * Created by braunster on 30/06/14.
 */
public class IncomingMessagesListener extends FirebaseGeneralEvent {

    private static final String TAG = IncomingMessagesListener.class.getSimpleName();
    private static final boolean DEBUG = Debug.IncomingMessagesListener;
    private Handler handler;

    private boolean isNew = false;
    private long creationTime;

    public IncomingMessagesListener(Handler handler){
        super(ChildEvent);
        this.handler = handler;
        creationTime = System.currentTimeMillis();
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
        if (DEBUG) Log.v(TAG, "Message has arrived, Alive: " + isAlive());
        if (isAlive())
            EventManager.MessageExecutor.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.v(TAG, "Message has arrived - execute, Alive: " + isAlive());
                    android.os.Process.setThreadPriority(/*isNew ? Process.THREAD_PRIORITY_BACKGROUND : */Process.THREAD_PRIORITY_BACKGROUND);
                    BMessage bmessage = (BMessage) BFirebaseInterface.objectFromSnapshot(dataSnapshot);

                    if (bmessage.getBUserSender() != null
                            &&
                            bmessage.getBUserSender().getId().longValue()
                                    != BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getId().longValue())
                    {
                        // Set the message as new if was told from creator,
                        // Or if the date of the message is later then the creation of this object.
                        if (isNew)
                            bmessage.setIsRead(false);
                        else if (creationTime < bmessage.getDate().getTime())
                            bmessage.setIsRead(false);

                        DaoCore.updateEntity(bmessage);
                    }

                    Message message = new Message();
                    message.what = 1;
                    message.obj = bmessage;
                    handler.sendMessageAtFrontOfQueue(message);
                }
            });
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
