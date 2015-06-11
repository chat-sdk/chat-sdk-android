package com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers;

import android.os.Handler;
import android.os.Message;

import com.braunster.androidchatsdk.firebaseplugin.firebase.FirebaseGeneralEvent;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.BUserConnection;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.AppEvents;
import com.braunster.chatsdk.network.BDefines;
import com.firebase.client.DataSnapshot;

import org.jdeferred.DoneCallback;

import java.util.Map;

import timber.log.Timber;

/**
 * Created by braunster on 08.06.15.
 */
public class BlockedUserListener extends FirebaseGeneralEvent{

    private BUser currentUser;
    private Handler handler;

    public BlockedUserListener(BUser currentUser, Handler handler) {
        super(ValueEvent);
        this.handler = handler;
        this.currentUser = currentUser;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        super.onChildAdded(dataSnapshot, s);

        Timber.v("onChildAdded");

        if (dataSnapshot.getValue() != null)
        {
            final BUser blockedUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class,
                    ((String) ((Map) dataSnapshot.getValue()).get(BDefines.Keys.BUID)));

            Timber.i("Blocked user was added, Entity Id: %s", blockedUser.getEntityID());

            BUserWrapper.initWithModel(blockedUser)
                    .metaOn().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void o) {
                    currentUser.connectUser(blockedUser, BUserConnection.Type.Blocked);

                    notifyChanged();
                }
            });
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        super.onChildRemoved(dataSnapshot);

        if (dataSnapshot.getValue() != null)
        {
            final BUser blockedUser = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class,
                    ((String) ((Map) dataSnapshot.getValue()).get(BDefines.Keys.BUID)));

            Timber.i("Blocked user was removed, EntityId: %s", blockedUser.getEntityID());

            currentUser.disconnectUser(blockedUser, BUserConnection.Type.Blocked);

            notifyChanged();
        }
    }

    private void notifyChanged(){
        Message message = new Message();
        message.what = AppEvents.BLOCKED_CHANGED;
        handler.sendMessageAtFrontOfQueue(message);
    }
}
