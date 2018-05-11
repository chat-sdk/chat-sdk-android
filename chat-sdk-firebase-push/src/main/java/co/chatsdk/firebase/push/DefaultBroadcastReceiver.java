package co.chatsdk.firebase.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import co.chatsdk.core.session.NM;
import co.chatsdk.ui.helpers.NotificationUtils;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.utils.AppBackgroundMonitor;

/**
 * Created by ben on 5/10/18.
 */

// We want to use this receiver if the app has been killed or if it's in the background
public class DefaultBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        final String threadEntityID = extras.getString(BaseInterfaceAdapter.THREAD_ENTITY_ID);
        final String userEntityID = extras.getString(BaseInterfaceAdapter.USER_ENTITY_ID);
        final String title = extras.getString("gcm.notification.title");
        final String body = extras.getString("gcm.notification.body");
        final String action = extras.getString("gcm.notification.click_action");

        // Only show the notification if the user is offline
        // This will be the case if the app
        // If the app is in the background
        Intent appIntent = null;
        if (!NM.auth().userAuthenticatedThisSession()) {
            appIntent = new Intent(context, InterfaceManager.shared().a.getLoginActivity());
        } else if (AppBackgroundMonitor.shared().inBackground() && NM.auth().userAuthenticatedThisSession()) {
            appIntent = new Intent(context, InterfaceManager.shared().a.getChatActivity());
        }
        if (appIntent != null) {
            appIntent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID, threadEntityID);
            appIntent.setAction(threadEntityID);
//            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NotificationUtils.createMessageNotification(context, appIntent, userEntityID, title, body);
        }

    }

}
