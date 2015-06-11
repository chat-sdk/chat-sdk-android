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
public class UserFriendsListener extends FirebaseGeneralEvent{

    private BUser currentUser;

    private Handler handler;

    public UserFriendsListener(BUser currentUser, Handler handler) {
        super(ValueEvent);
        this.handler = handler;
        this.currentUser = currentUser;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        super.onChildAdded(dataSnapshot, s);

        if (dataSnapshot.getValue() != null)
        {
            final BUser friend = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class,
                    ((String) ((Map) dataSnapshot.getValue()).get(BDefines.Keys.BUID)));

            BUserWrapper.initWithModel(friend)
                    .metaOn().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void o) {
                    Timber.d("Friend was added, EntityId: %s", friend.getEntityID());

                    currentUser.connectUser(friend, BUserConnection.Type.Friend);

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
            final BUser friend = DaoCore.fetchOrCreateEntityWithEntityID(BUser.class,
                    ((String) ((Map) dataSnapshot.getValue()).get(BDefines.Keys.BUID)));

            currentUser.disconnectUser(friend, BUserConnection.Type.Friend);

            Timber.d("Friend was removed, EntityId: %s", friend.getEntityID());

            notifyChanged();
        }
    }

    private void notifyChanged(){
        Message message = new Message();
        message.what = AppEvents.FRIENDS_CHANGED;
        handler.sendMessage(message);
    }
}
