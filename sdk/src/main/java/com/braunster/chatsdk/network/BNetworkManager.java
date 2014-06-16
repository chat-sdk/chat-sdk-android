package com.braunster.chatsdk.network;


import android.location.LocationManager;
import android.widget.ImageView;

import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
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

    private static HashSet<ActivityListener> listeners = new HashSet<ActivityListener>();

    private static List<ActivityListener> activityListeners= new ArrayList<ActivityListener>();

    private static AbstractNetworkAdapter networkAdapter;

    /* Added set network adapter*/
    public static void syncWithProgress(CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.syncWithProgress(completionListener);
    }

    public static void getFriendsListWithListener(CompletionListenerWithData completionListenerWithData) {
        if (networkAdapter != null)
            networkAdapter.getFriendsListWithListener(completionListenerWithData);
    }

    public static BUser currentUser() {
        if (networkAdapter != null)
            return networkAdapter.currentUser();
        else return null;
    }

    public static void createThreadWithUsers(ArrayList<BUser> users, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.createThreadWithUsers(users, completionListener);
    }

    public static void createPublicThreadWithName(String name, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.createPublicThreadWithName(name, completionListener);
    }

    public static void setLastOnline(Date lastOnline) {
        if (networkAdapter != null)
            networkAdapter.setLastOnline(lastOnline);
    }

    public static void deleteThreadWithEntityID(String entityId, final CompletionListener completionListener) {
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

    public static ArrayList<BThread> threadsWithType(BThread.Type type) {
        if (networkAdapter != null)
            return networkAdapter.threadsWithType(type);
        else return null;
    }

    public static String serverURL() {
        if (networkAdapter != null)
            return networkAdapter.serverURL();
        return ""; // Or should i return null, Will see in the future.
    }

    public static void sendMessageWithText(String text, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithText(text, threadEntityId, completionListener);
     }

    public static void sendMessageWithImage(ImageView imageView, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithImage(imageView, threadEntityId, completionListener);
    }

    public  static void sendMessageWithLocation(LocationManager locationManager, String threadEntityId, CompletionListener completionListener) {
        if (networkAdapter != null)
            networkAdapter.sendMessageWithLocation(locationManager, threadEntityId, completionListener);
    }

    public static void save() {
        if (networkAdapter != null)
            networkAdapter.save();
    }

    // TODO add order veriable for the data.
    public static List<BMessage> getMessagesForThreadForEntityID(String entityId) {
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

    public static void setNetworkAdapter(AbstractNetworkAdapter adapter) {
        networkAdapter = adapter;
    }

    public static void setNewDataListener(ActivityListener newDataListener) {

    }

    public static void addActivityListener(ActivityListener activityListener){
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
