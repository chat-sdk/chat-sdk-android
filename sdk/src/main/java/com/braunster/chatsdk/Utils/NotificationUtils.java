package com.braunster.chatsdk.Utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.network.BDefines;

import org.apache.commons.lang3.StringUtils;

import java.util.MissingResourceException;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.TEXT;

/**
 * Created by braunster on 01/07/14.
 */
public class NotificationUtils {


    public static final int NOTIFICATION_CONNECTION_ID = 1991;
    public static final int NOTIFICATION_ALERT_ID = 1990;

    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String TITLE = "title";
    public static final String TICKER = "ticker";
    public static final String CONTENT = "content";
    public static final String NOT_TAG = "tag";

    /** Create and alert notification that the connection has lost.*/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data){
        createAlertNotification(context, id, resultIntent, data, android.R.drawable.ic_notification_overlay, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), -1);
    }

    public static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data, int smallIconResID, Uri soundUri, int number){
        String title, content;

        if (DEBUG) Log.i(TAG, "createAlertNotification, ID: " + id + ", Number: " + number);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        id,
                        resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                );

        if (data.getString(TITLE) != null)
            title = data.getString(TITLE);
        else throw new MissingResourceException("you must have a title for creating notification.", NotificationUtils.class.getSimpleName(), TITLE);

        if (data.getString(CONTENT) != null)
            content = data.getString(CONTENT);
        else throw new MissingResourceException("you must have a content for creating notification.", NotificationUtils.class.getSimpleName(), CONTENT);

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(smallIconResID)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setLights(0xFF0000FF, 500, 3000)
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setSound(soundUri)
                        .setNumber(number)
                        .setContentIntent(resultPendingIntent);

        if (data.getString(TICKER) != null)
            mBuilder.setTicker(data.getString(TICKER));

        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = mBuilder.getNotification();
        else
            notification = mBuilder.build();

        notification.flags = Notification.FLAG_AUTO_CANCEL ;

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(id, notification);
    }

    public static void createMessageNotification(Context context, BMessage message){
        createMessageNotification(context, message, android.R.drawable.ic_notification_overlay, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), -1);
    }

    public static void createMessageNotification(Context context, BMessage message, int smallIconResID, Uri soundUri, int number){
        createMessageNotification(context, BDefines.MESSAGE_NOTIFICATION_ID, message, smallIconResID, soundUri, number);
    }

    public static void createMessageNotification(Context context, int id, BMessage message, int smallIconResID, Uri soundUri, int number){
        if (DEBUG) Log.v(TAG, "createMessageNotification");

        Intent resultIntent = getChatResultIntent(context);
        resultIntent.putExtra(ChatSDKChatActivity.THREAD_ID,  message.getOwnerThread());
        resultIntent.putExtra(ChatSDKChatActivity.FROM_PUSH, true);
        resultIntent.putExtra(ChatSDKChatActivity.MSG_TIMESTAMP, message.getDate().getTime());

        String msgContent = message.getType() == TEXT ? message.getText() : message.getType() == IMAGE ? "Image" : "Location";

        String title = !StringUtils.isEmpty(
                message.getBUserSender().getMetaName()) ? message.getBUserSender().getMetaName() : " ";

        Bundle data = NotificationUtils.getDataBundle(title, "New message from " + message.getBUserSender().getMetaName(), msgContent);

        createAlertNotification(context, id, resultIntent, data, smallIconResID, soundUri, number);
    }

    private static Intent getChatResultIntent(Context context){
        return new Intent(context, ChatSDKUiHelper.getInstance().mainActivity);
    }

    /** Cancel the ongoing notification that controls the connection state and play/stop*/
    public static void cancelNotification(Context context, int id){
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    public static void cancelNotification(Context context, String tag, int id){
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.cancel(tag, id);
    }

    public static Bundle getDataBundle(String title, String ticker, String content){
        Bundle data = new Bundle();

        if (StringUtils.isNotEmpty(title))
            data.putString(TITLE, title);
        else throw new MissingResourceException("you must have a title for creating notification.", NotificationUtils.class.getSimpleName(), TITLE);

        if (StringUtils.isNotEmpty(content))
            data.putString(CONTENT, content);
        else throw new MissingResourceException("you must have a content for creating notification.", NotificationUtils.class.getSimpleName(), CONTENT);

        if (StringUtils.isNotEmpty(ticker))
            data.putString(TICKER, ticker);

        return data;
    }

}
