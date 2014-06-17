package com.braunster.chatsdk.network;


import android.content.Context;
import android.location.LocationManager;
import android.util.Log;
import android.widget.ImageView;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.DaoCore;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class BNetworkManager implements ActivityListener {

    private static final String TAG = BNetworkManager.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static BNetworkManager instance;

    private HashSet<ActivityListener> listeners = new HashSet<ActivityListener>();

    private List<ActivityListener> activityListeners= new ArrayList<ActivityListener>();

    private AbstractNetworkAdapter networkAdapter;

    public static void init(Context ctx){
        DaoCore.init(ctx);
    }

    public static BNetworkManager getInstance(){
        if (DEBUG) Log.v(TAG, "getInstance");
        if (instance == null) {
            instance = new BNetworkManager();
        }
        return instance;
    }

    /* Added set network adapter*/
    public void syncWithProgress(CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.syncWithProgress(completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void getFriendsListWithListener(CompletionListenerWithData completionListenerWithData) {
        if (networkAdapter != null)
            networkAdapter.getFriendsListWithListener(completionListenerWithData);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public BUser currentUser() {
        if (networkAdapter != null)
            return networkAdapter.currentUser();
        else
        {
            if (DEBUG) Log.e(TAG, "Network adapter is null");
            return null;
        }
    }

    public void createThreadWithUsers(List<BUser> users, CompletionListenerWithData<String> completionListener) {
        if (networkAdapter != null)
            networkAdapter.createThreadWithUsers(users, completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void createThreadWithUsers(CompletionListenerWithData<String>  completionListener, BUser...users) {
        if (networkAdapter != null)
            networkAdapter.createThreadWithUsers(Arrays.asList(users), completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void createPublicThreadWithName(String name, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.createPublicThreadWithName(name, completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void setLastOnline(Date lastOnline) {
        if (networkAdapter != null)
            networkAdapter.setLastOnline(lastOnline);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void deleteThreadWithEntityID(String entityId, final CompletionListener completionListener) {
        if (networkAdapter != null)
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
        else if (DEBUG) Log.e(TAG, "Network adapter is null");

    }

    public ArrayList<BThread> threadsWithType(BThread.Type type) {
        if (networkAdapter != null)
            return networkAdapter.threadsWithType(type);
        else
        {
            if (DEBUG) Log.e(TAG, "Network adapter is null");
            return null;
        }
    }

    public String serverURL() {
        if (networkAdapter != null)
            return networkAdapter.serverURL();
        return ""; // Or should i return null, Will see in the future.
    }

    public void sendMessageWithText(String text, String threadEntityId, CompletionListenerWithData<BMessage> completionListener) {
        if (DEBUG) Log.v(TAG, "sendMessageWithText");
        if (networkAdapter != null)
            networkAdapter.sendMessageWithText(text, threadEntityId, completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
     }

    public void sendMessageWithImage(ImageView imageView, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithImage(imageView, threadEntityId, completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void sendMessageWithLocation(LocationManager locationManager, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithLocation(locationManager, threadEntityId, completionListener);
        else if (DEBUG) Log.e(TAG, "Network adapter is null");
    }

    public void save() {
        if (networkAdapter != null)
            networkAdapter.save();
    }

    // TODO add order veriable for the data.
    public List<BMessage> getMessagesForThreadForEntityID(String entityId) {
        if (networkAdapter != null)
            return networkAdapter.getMessagesForThreadForEntityID(entityId);
        else
        {
            if (DEBUG) Log.e(TAG, "Network adapter is null");
            return null;
        }
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

    public void setNetworkAdapter(AbstractNetworkAdapter adapter) {
        networkAdapter = adapter;
        networkAdapter.setActivityListener(this);
    }

    public ActivityListener addActivityListener(ActivityListener activityListener){
        if (!listeners.contains(activityListener))
            listeners.add(activityListener);

        return activityListener;
    }

    public void removeActivityListener(ActivityListener activityListener){
        listeners.remove(activityListener);
    }

    public AbstractNetworkAdapter getNetworkAdapter() {
        return networkAdapter;
    }

    public void dispatchMessageAdded(BMessage message){
        for (ActivityListener a : listeners)
            a.onMessageAdded(message);
    }

    public void dispatchThreadAdded(BThread thread){
        for (ActivityListener a : listeners)
            a.onThreadAdded(thread);
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
