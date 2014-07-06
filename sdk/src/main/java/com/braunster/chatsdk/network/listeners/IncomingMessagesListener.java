package com.braunster.chatsdk.network.listeners;

import android.os.Handler;
import android.os.Message;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.network.events.FirebaseGeneralEvent;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.firebase.client.DataSnapshot;


/**
 * Created by braunster on 30/06/14.
 */
public class IncomingMessagesListener extends FirebaseGeneralEvent {

    private static final String TAG = IncomingMessagesListener.class.getSimpleName();
    private static final boolean DEBUG = true;
    private Handler handler;

    public IncomingMessagesListener(Handler handler){
        super(ChildEvent);
        this.handler = handler;
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BMessage bmessage = (BMessage) BFirebaseInterface.objectFromSnapshot(dataSnapshot);
                Message message = new Message();
                message.what = 1;
                message.obj = bmessage;
                handler.sendMessage(message);
            }
        }).start();
//        EventManager.getInstance().onMessageReceived(message);
    }
}
