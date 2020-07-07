package sdk.chat.core.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;

public class BaseBroadcastHandler implements BroadcastHandler {

    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if(!ChatSDK.shared().isValid() || !ChatSDK.config().inboundPushHandlingEnabled || !ChatSDK.push().enabled() || extras == null) {
            return;
        }

        final String threadEntityID = extras.getString(Keys.PushKeyThreadEntityID);
        final String userEntityID = extras.getString(Keys.PushKeyUserEntityID);
        final String title = extras.getString(Keys.PushKeyTitle);
        final String body = extras.getString(Keys.PushKeyBody);

        // Check if notifications are muted
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        if (thread != null) {
            if (thread.isMuted()) {
                return;
            }
        }

        // Only show the notification if the user is offline
        // This will be the case if the app
        // If the app is in the background
        Intent appIntent = null;
        if (ChatSDK.auth() == null || !ChatSDK.auth().isAuthenticatedThisSession() || ChatSDK.config().backgroundPushTestModeEnabled) {
            appIntent = new Intent(context, ChatSDK.ui().getSplashScreenActivity());
        }
        else if (AppBackgroundMonitor.shared().inBackground() && ChatSDK.auth().isAuthenticatedThisSession()) {
            appIntent = new Intent(context, ChatSDK.ui().getChatActivity());
        }
        if (appIntent != null) {
            appIntent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
            appIntent.setAction(threadEntityID);
//            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            ChatSDK.ui().notificationDisplayHandler().createMessageNotification(context, appIntent, userEntityID, threadEntityID, title, body);
        }

    }
}