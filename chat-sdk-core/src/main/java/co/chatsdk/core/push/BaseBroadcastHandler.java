package co.chatsdk.core.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.utils.AppBackgroundMonitor;

public class BaseBroadcastHandler implements BroadcastHandler {

    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if(ChatSDK.config() == null || !ChatSDK.config().inboundPushHandlingEnabled) {
            return;
        }

        final String threadEntityID = extras.getString(Keys.THREAD_ENTITY_ID);
        final String userEntityID = extras.getString(Keys.USER_ENTITY_ID);
        final String title = extras.getString(Keys.PUSH_TITLE);
        final String body = extras.getString(Keys.PUSH_BODY);

        // Only show the notification if the user is offline
        // This will be the case if the app
        // If the app is in the background
        Intent appIntent = null;
        if (ChatSDK.auth() == null || !ChatSDK.auth().isAuthenticatedThisSession() || ChatSDK.config().backgroundPushTestModeEnabled) {
            appIntent = new Intent(context, ChatSDK.ui().getSplashScreenActivity());
        } else if (AppBackgroundMonitor.shared().inBackground() && ChatSDK.auth().isAuthenticatedThisSession()) {
            appIntent = new Intent(context, ChatSDK.ui().getChatActivity());
        }
        if (appIntent != null) {
            appIntent.putExtra(Keys.THREAD_ENTITY_ID, threadEntityID);
            appIntent.setAction(threadEntityID);
//            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ChatSDK.ui().notificationDisplayHandler().createMessageNotification(context, appIntent, userEntityID, title, body);
        }

    }
}