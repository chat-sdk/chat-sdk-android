/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.utils.ImageBuilder;

public class NotificationUtils {

    public static final int MESSAGE_NOTIFICATION_ID = 1001;

    public static void createMessageNotification(Context context, Message message) {

        String threadID = message.getThread().getEntityID();

        Intent openChatIntent = new Intent(context, InterfaceManager.shared().a.getChatActivity());
        openChatIntent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID, threadID);
        openChatIntent.setAction(threadID);
        openChatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        createMessageNotification(context, openChatIntent, message.getSender().getEntityID(), message.getSender().getName(), message.getTextString());

    }

    public static void createMessageNotification(final Context context, Intent resultIntent, String userEntityID, String title, String message) {

        int pushIcon = ChatSDK.config().pushNotificationImageDefaultResourceId;
        if(pushIcon <= 0) {
            pushIcon = R.drawable.icn_72_push_mask;
        }
        final int smallPushIcon = pushIcon;

        final Bitmap largePushIcon = BitmapFactory.decodeResource(context.getResources(), smallPushIcon);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (userEntityID != null && !userEntityID.isEmpty()) {
            User user = StorageManager.shared().fetchUserWithEntityID(userEntityID);
            if (user != null) {
                ImageBuilder.bitmapForURL(context, user.getAvatarURL()).subscribe((bitmap, throwable) -> {
                    if (throwable != null) {
                        ChatSDK.logError(throwable);
                    }
                    if (bitmap == null) {
                        bitmap = largePushIcon;
                    }
                    createAlertNotification(context, resultIntent, title, message, bitmap, smallPushIcon, alarmSound, -1);
                });
            } else {
                createAlertNotification(context, resultIntent, title, message, largePushIcon, smallPushIcon, alarmSound, -1);
            }
        }
    }

    /**
     * @param context
     * @param resultIntent
     * @param title
     * @param message
     * @param largeIcon
     * @param smallIconResID
     * @param soundUri
     * @param number - Number of notifications represented by this alert
     */
    public static void createAlertNotification(Context context, Intent resultIntent, String title, String message, Bitmap largeIcon, int smallIconResID, Uri soundUri, int number){

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder =
                new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(smallIconResID)
//                        .setLights(0xFF0000FF, 500, 3000)
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setSound(soundUri)
                        .setNumber(number)
                        .setContentIntent(pendingIntent)
                        .setTicker(title + ": " + message)
                        .setPriority(Notification.PRIORITY_HIGH);

        if (largeIcon != null) {
            Notification.InboxStyle style = new Notification.InboxStyle()
                    .setBigContentTitle(title)
                    .setSummaryText(message);

            builder.setStyle(style);
            builder.setLargeIcon(ImageUtils.scaleImage(largeIcon, (int) (context.getResources().getDisplayMetrics().density * 48)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(context.getResources().getColor(R.color.chat_blue));
        }

        Notification notification = builder.build();

        notification.flags = Notification.FLAG_AUTO_CANCEL ;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);

        wakeScreen(context);
    }

    /**
     * Waking up the screen
     * * * */
    private static void wakeScreen(Context context){

        // Waking the screen so the user will see the notification
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);


        boolean isScreenOn;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
            isScreenOn = pm.isScreenOn();
        else
            isScreenOn = pm.isInteractive();

        if(!isScreenOn)
        {

            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                    |PowerManager.ON_AFTER_RELEASE
                    |PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyLock");

            wl.acquire(5000);
            wl.release();
        }
    }
}
