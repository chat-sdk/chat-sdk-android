package com.braunster.chatsdk.network;


import android.location.LocationManager;
import android.widget.ImageView;

import com.braunster.chatsdk.entities.BMessage;
import com.braunster.chatsdk.entities.BThread;
import com.braunster.chatsdk.entities.BUser;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class BNetworkManager implements ActivityListener {

    private static final String TAG = BNetworkManager.class.getSimpleName();
    private static final boolean DEBUG = false;

    private HashSet<ActivityListener> listeners = new HashSet<ActivityListener>();

    private List<ActivityListener> activityListeners= new ArrayList<ActivityListener>();

    private AbstractNetworkAdapter networkAdapter;

    /* Added set network adapter*/
    public void syncWithProgress(CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.syncWithProgress(completionListener);
    }

    public void getFriendsListWithListener(CompletionListenerWithData completionListenerWithData) {
        if (networkAdapter != null)
            networkAdapter.getFriendsListWithListener(completionListenerWithData);
    }

    public BUser currentUser() {
        if (networkAdapter != null)
            return networkAdapter.currentUser();
        else return null;
    }

    public void createThreadWithUsers(ArrayList<BUser> users, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.createThreadWithUsers(users, completionListener);
    }

    public void createPublicThreadWithName(String name, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.createPublicThreadWithName(name, completionListener);
    }

    public void setLastOnline(Date lastOnline) {
        if (networkAdapter != null)
            networkAdapter.setLastOnline(lastOnline);
    }

    public void deleteThreadWithEntityID(String entityId, final CompletionListener completionListener) {
        if (networkAdapter != null)
        {
            networkAdapter.deleteThreadWithEntityID(entityId, new CompletionListener() {
                @Override
                public void onDone() {
                    // TODO save the database locally.
                    completionListener.onDone();
                }

                @Override
                public void onDoneWithError() {
                    completionListener.onDoneWithError();
                }
            });
        }
    }

    public ArrayList<BThread> threadsWithType(BThread.threadType type) {
        if (networkAdapter != null)
            return networkAdapter.threadsWithType(type);
        else return null;
    }

    public String serverURL() {
        if (networkAdapter != null)
            return networkAdapter.serverURL();
        return ""; // Or should i return null, Will see in the future.
    }

    public void sendMessageWithText(String text, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithText(text, threadEntityId, completionListener);
     }

    public void sendMessageWithImage(ImageView imageView, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithImage(imageView, threadEntityId, completionListener);
    }

    public void sendMessageWithLocation(LocationManager locationManager, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithLocation(locationManager, threadEntityId, completionListener);
    }

    public void save() {
        if (networkAdapter != null)
            networkAdapter.save();
    }

    // TODO add order veriable for the data.
    public ArrayList<BMessage> getMessagesForThreadForEntityID(String entityId) {
        if (networkAdapter != null)
            return networkAdapter.getMessagesForThreadForEntityID(entityId);
        else return null;
    }

    @Override
    public void onThreadAdded(BThread thread) {
        for (ActivityListener l : listeners)
            l.onThreadAdded(thread);
    }

    @Override
    public void onMessageAdded(BMessage message) {
        for (ActivityListener l : listeners)
            l.onMessageAdded(message);
    }

    public void setNetworkAdapter(AbstractNetworkAdapter networkAdapter) {
        this.networkAdapter = networkAdapter;
    }

    public void setNewDataListener(ActivityListener newDataListener) {

    }

    public void addActivityListener(ActivityListener activityListener){
        if (!listeners.contains(activityListener))
            listeners.add(activityListener);
    }

    public void removeActivityListener(ActivityListener activityListener){
        listeners.remove(activityListener);
    }


    /* ASK -(void) application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    if (_networkAdapter) {
        if ([_networkAdapter respondsToSelector:@selector(application:didReceiveRemoteNotification:)]) {
            [_networkAdapter application:application didReceiveRemoteNotification:userInfo];
        }
    }
}

-(void) application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    if (_networkAdapter) {
        if ([_networkAdapter respondsToSelector:@selector(application:didRegisterForRemoteNotificationsWithDeviceToken:)]) {
            [_networkAdapter application:application didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
        }
    }
}*/
}
