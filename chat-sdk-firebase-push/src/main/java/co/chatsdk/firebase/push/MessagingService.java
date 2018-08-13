package co.chatsdk.firebase.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;

import com.example.firebasepushnotifications.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.manager.InterfaceManager;
import timber.log.Timber;

/**
 * Created by ben on 9/1/17.
 */

public class MessagingService extends FirebaseMessagingService {

    public static String ChatSDKMessageChannel = "co.chatsdk.notification.Message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Timber.v("Received push");

        // This isn't actually needed becuase we're using the default push notification

        String threadEntityID = remoteMessage.getData().get(BaseInterfaceAdapter.THREAD_ENTITY_ID);

        // Get the title
        String title = "";
        String body = "";

        if (remoteMessage.getData() != null) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        int pushIcon = ChatSDK.config().pushNotificationImageDefaultResourceId;
        if(pushIcon <= 0) {
            pushIcon = R.drawable.push_icon;
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(pushIcon)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setSound(alarmSound)
//                .setAutoCancel(true)
//                .setChannelId(ChatSDKMessageChannel);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(pushIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(Notification.PRIORITY_MAX)
                .setSound(alarmSound)
                .setAutoCancel(true);


        // Tap Action
        if (threadEntityID != null && threadEntityID.length() > 0) {
            Intent intent = new Intent(this, InterfaceManager.shared().a.getChatActivity());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID, threadEntityID);

            PendingIntent pendingIntent = TaskStackBuilder.create(this)
                    .addParentStack(InterfaceManager.shared().a.getMainActivity())
                    .addNextIntent(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);
        }

//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationManager manager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.push_channel_name);
            NotificationChannel channel = new NotificationChannel(ChatSDKMessageChannel, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setDescription(description);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

        }

        manager.notify(0, builder.build());

    }

}
