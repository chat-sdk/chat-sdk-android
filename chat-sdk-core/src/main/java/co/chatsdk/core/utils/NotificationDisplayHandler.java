/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.utils;

import android.app.Notification;
import android.app.NotificationChannel;
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

import co.chatsdk.core.R;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.MessageDisplayHandler;
import co.chatsdk.core.session.ChatSDK;

public class NotificationDisplayHandler {

    public static final int MESSAGE_NOTIFICATION_ID = 1001;
    public static String ChatSDKMessageChannel = "co.chatsdk.notification.Message";

    public void createMessageNotification(Message message) {

        Context context = ChatSDK.shared().context();
        String threadID = message.getThread().getEntityID();

        Intent openChatIntent = new Intent(context, ChatSDK.ui().getChatActivity());
        openChatIntent.putExtra(Keys.IntentKeyThreadEntityID, threadID);
        openChatIntent.setAction(threadID);
        openChatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        MessageDisplayHandler displayHandler = ChatSDK.ui().getMessageHandler(message.getMessageType());

        createMessageNotification(context, openChatIntent, message.getSender().getEntityID(), message.getSender().getName(), displayHandler.displayName(message));

    }

    public void createMessageNotification(final Context context, Intent resultIntent, String userEntityID, String title, String message) {

        int pushIcon = ChatSDK.config().pushNotificationImageDefaultResourceId;
        if(pushIcon <= 0) {
            pushIcon = R.drawable.icn_72_push_mask;
        }
        final int smallPushIcon = pushIcon;

        final Bitmap largePushIcon = BitmapFactory.decodeResource(context.getResources(), smallPushIcon);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (userEntityID != null && !userEntityID.isEmpty()) {
            User user = ChatSDK.db().fetchUserWithEntityID(userEntityID);
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
    public void createAlertNotification(Context context, Intent resultIntent, String title, String message, Bitmap largeIcon, int smallIconResID, Uri soundUri, int number){

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder =
                new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(smallIconResID)
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setSound(soundUri)
                        .setNumber(number)
                        .setContentIntent(pendingIntent)
                        .setTicker(title + ": " + message)
                        .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (largeIcon != null) {
            builder.setLargeIcon(ImageUtils.scaleImage(largeIcon, (int) (context.getResources().getDisplayMetrics().density * 48)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ChatSDK.config().pushNotificationColor);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            builder.setChannelId(ChatSDKMessageChannel);

            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.push_channel_name);

            NotificationChannel channel = new NotificationChannel(ChatSDKMessageChannel, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

        }


        Notification notification = builder.build();

        notification.flags = Notification.FLAG_AUTO_CANCEL ;

        notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);

        wakeScreen(context);
    }

    /**
     * Waking up the screen
     * * * */
    private void wakeScreen(Context context){

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
                    |PowerManager.ACQUIRE_CAUSES_WAKEUP, "chat-sdk:MyLock");

            wl.acquire(5000);
            wl.release();
        }
    }
}
