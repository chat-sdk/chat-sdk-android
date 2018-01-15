package co.chatsdk.firebase.push;

import android.app.Notification;
import android.app.NotificationManager;

import com.example.firebasepushnotifications.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import co.chatsdk.core.session.ChatSDK;
import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.v("Received push");

        // Get the user


        int pushIcon = ChatSDK.config().pushNotificationImageDefaultResourceId;
        if(pushIcon <= 0) {
            pushIcon = R.drawable.push_icon;
        }

        Notification n  = new Notification.Builder(this)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setSmallIcon(pushIcon)

//                .setSmallIcon()
//                .setSmallIcon(R.drawable.icon)
//                .setContentIntent(pIntent)
//                .setAutoCancel(true)
//                .addAction(R.drawable.icon, "Call", pIntent)
//                .addAction(R.drawable.icon, "More", pIntent)
//                .addAction(R.drawable.icon, "And more", pIntent).
 .build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);


    }

}
