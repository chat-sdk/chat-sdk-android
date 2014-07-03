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

import java.util.MissingResourceException;

/**
 * Created by braunster on 01/07/14.
 */
public class NotificationUtils {


    public static final int NOTIFICATION_CONNECTION_ID = 1991;
    public static final int NOTIFICATION_ALERT_ID = 1990;

    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String TITLE = "title";
    public static final String TICKER = "ticker";
    public static final String CONTENT = "content";

    /** Create and alert notification that the connection has lost.*/
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data){
        String title, content;

        if (DEBUG) Log.i(TAG, "createAlertNotification, ID: " + id);

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

        //Define sound URI - adding sound to the notification.
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setLights(0xFF0000FF, 500, 3000)
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setSound(soundUri)
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

        // Builds the notification and issues it.
        mNotifyMgr.notify(id, notification);
    }

    /** Create an ongoing notification that can terminate the connection or play/stop the sound directly from the notification drawer.*/
    //region Ongoing Notification
/*  public static void  createConnectedNotification(Context context, boolean isStreaming, boolean isServer){

        if(DEBUG) Log.d(TAG, "createConnectedNotification, " + (isStreaming ? "Streaming" : "not streaming") );

        // Build the notification characteristic.
        Notification.Builder mBuilder;
        mBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.disconnect_btn);

        // The view for the notification
        RemoteViews contentView= new RemoteViews(context.getPackageName(), R.layout.notification_running_layout);

        // Listener for disconnect button
        Intent disconnectIntent =new Intent(TCPConnection.ACTION_CLOSE_CONNECTION);
        PendingIntent disconnectPendingIntent = PendingIntent.getBroadcast(context, 1, disconnectIntent, 0);
        contentView.setOnClickPendingIntent(R.id.btn_disconnect, disconnectPendingIntent);

        // Listener for play/pause button
        Intent playStopIntent =new Intent(TCPConnection.ACTION_TOGGLE_CONTROLLER);
        // Extra which controller to use. Server use sound player client us recorder
        playStopIntent.putExtra(TCPConnection.CONTROLLER,
                isServer ? TCPConnection.CONTROLLER_SOUND_PLAYER : TCPConnection.CONTROLLER_SOUND_RECORDER);
        // The action to do, opposite from the current state so if streaming, stop streaming.
        playStopIntent.putExtra(TCPConnection.CONTROLLER_ACTION, !isStreaming);

        PendingIntent playStopPendingIntent = PendingIntent.getBroadcast(context, 1, playStopIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (isStreaming)
            contentView.setImageViewResource(R.id.btn_controller, R.drawable.stop_btn);
        else
            contentView.setImageViewResource(R.id.btn_controller, R.drawable.play_btn);

        contentView.setOnClickPendingIntent(R.id.btn_controller, playStopPendingIntent);

        // Listener for the text message
        Intent messageIntent =new Intent(context, MonitorActivity.class);
        PendingIntent messagePendingIntent = PendingIntent.getActivity(context, 1, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        contentView.setOnClickPendingIntent(R.id.txt_message, messagePendingIntent);

        // Notification Object from Builder
        Notification notification;

        if (Build.VERSION.SDK_INT < 16)
            notification = mBuilder.getNotification();
        else
            notification = mBuilder.build();

        // Add flag of ongoing event
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        // Set the content view of the notification to the xml.
        notification.contentView = contentView;

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.

        mNotifyMgr.notify(NOTIFICATION_CONNECTION_ID, notification);
    }*/
    //endregion

    /** Cancel the ongoing notification that controls the connection state and play/stop*/
    public static void cancelNotification(Context context, int id){
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    public static Bundle getDataBundle(String title, String ticker, String content){
        Bundle data = new Bundle();

        if (title != null)
            data.putString(TITLE, title);
        else throw new MissingResourceException("you must have a title for creating notification.", NotificationUtils.class.getSimpleName(), TITLE);

        if (content != null)
            data.putString(CONTENT, content);
        else throw new MissingResourceException("you must have a content for creating notification.", NotificationUtils.class.getSimpleName(), CONTENT);

        if (ticker != null)
            data.putString(TICKER, ticker);

        return data;
    }

}
