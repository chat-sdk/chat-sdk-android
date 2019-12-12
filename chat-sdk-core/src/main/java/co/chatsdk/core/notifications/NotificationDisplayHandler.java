/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.core.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;
import co.chatsdk.core.dao.Thread;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class NotificationDisplayHandler implements Consumer<Throwable> {

    public static final int MESSAGE_NOTIFICATION_ID = 1001;

    public Disposable createMessageNotification(Message message) {

        final Context context = ChatSDK.shared().context();

        if (connectedToAuto(context)) {
            return new NotificationBuilder(context).forMessageAuto(message).build().subscribe(builder -> {
                        NotificationManagerCompat.from(context).notify(MESSAGE_NOTIFICATION_ID, builder.build());
                    }, this);
        } else {
            return new NotificationBuilder(context).forMessage(message).build().subscribe(builder -> {
                Notification notification = builder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
                wakeScreen(context);
            }, this);
        }

    }

    public Disposable createMessageNotification(final Context context, Intent resultIntent, String userEntityID, String threadEntityId, String title, String message) {
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityId);
        if (connectedToAuto(context) && thread != null) {
            return new NotificationBuilder(context).forAuto(title, message, thread).build().subscribe(builder -> {
                NotificationManagerCompat.from(context).notify(MESSAGE_NOTIFICATION_ID, builder.build());
            }, this);
        } else {
            NotificationBuilder builder = new NotificationBuilder(context)
                    .useDefault()
                    .setIntent(resultIntent)
                    .addIconForUserEntityID(userEntityID)
                    .setTitle(title)
                    .setText(message);
            if (thread != null && ChatSDK.config().replyFromNotificationEnabled) {
                builder = builder.addMessageReplyActionsForThread(thread);
            }
            return builder.build().subscribe(b -> {
                Notification notification = b.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(MESSAGE_NOTIFICATION_ID, notification);
                wakeScreen(context);
            }, this);
        }
    }

    /**
     * Waking up the screen
     * * * */
    protected void wakeScreen(Context context){

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

    public static boolean connectedToAuto(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
            return true;
        }
        return false;
    }

    @Override
    public void accept(Throwable t) throws Exception {
        t.printStackTrace();
    }
}
