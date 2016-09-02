/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.asynctask.MakeThreadImage;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import timber.log.Timber;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.TEXT;

public class NotificationUtils {


    public static final int NOTIFICATION_CONNECTION_ID = 1991;
    public static final int NOTIFICATION_ALERT_ID = 1990;

    private static final String TAG = NotificationUtils.class.getSimpleName();
    private static final boolean DEBUG = Debug.NotificationUtils;

    public static final String TITLE = "title";
    public static final String TICKER = "ticker";
    public static final String CONTENT = "content";
    public static final String LINES = "lines";
    public static final String SUMMARY= "summary";
    public static final String NOT_TAG = "tag";


    private static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data, int smallIconResID, Uri soundUri, int number){
       createAlertNotification(context, id, resultIntent, data, null, smallIconResID, soundUri, number);
    }

    private static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data, Bitmap bitmap, int smallIconResID, Uri soundUri, int number){
        String title, content;

        if (DEBUG) Timber.i("createAlertNotification, ID: %s, Number: %s", id, number);

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
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(smallIconResID)
                        .setLights(0xFF0000FF, 500, 3000)
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setSound(soundUri)
                        .setNumber(number)
                        .setContentIntent(resultPendingIntent);

        if (data.getString(TICKER) != null)
            mBuilder.setTicker(data.getString(TICKER));

        if (bitmap != null)
        {
            if (Build.VERSION.SDK_INT >= 16)
            {
                Notification.InboxStyle style = new Notification.InboxStyle()
                        .setBigContentTitle(title)
                        .setSummaryText(content);


                // Adding the lines to the notification
                if (data.containsKey(LINES))
                {
                    ArrayList<String> list = data.getStringArrayList(LINES);

                    if (list != null && list.size()>0) {

                        if (DEBUG) Timber.d("Contains lines: %s, listSize: %s", data.containsKey(LINES), list.size());

                        for (String s : list)
                        {
                            if (DEBUG) Timber.d("Line Added: %s", s);
                            style.addLine(s);
                        }
                    }
                }

                // ADding notification summary
                if (data.containsKey(SUMMARY))
                    style.setSummaryText(data.getString(SUMMARY));
                
                mBuilder.setStyle(style);
            }
            
            mBuilder.setLargeIcon(ImageUtils.scaleImage(bitmap, (int) (context.getResources().getDisplayMetrics().density * 48)));

        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mBuilder.setColor(context.getResources().getColor(R.color.accent_material_dark));
        }

        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = mBuilder.getNotification();
        else {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
            notification = mBuilder.build();
        }

        notification.flags = Notification.FLAG_AUTO_CANCEL ;

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(id, notification);

        wakeScreen(context);
    }




    /** Create and alert notification that the connection has lost.*/
    public static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data){
        createAlertNotification(context, id, resultIntent, data, R.drawable.ic_launcher, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), -1);
    }



    public static void createMessageNotification(Context context, BMessage message){
        createMessageNotification(context, message, R.drawable.ic_launcher, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), -1);
    }
    
    public static void createMessageNotification(Context context, BMessage message, int smallIconResID, Uri soundUri, int number){
        createMessageNotification(context, BDefines.MESSAGE_NOTIFICATION_ID, message, smallIconResID, soundUri, number);
    }

    public static void createMessageNotification(final Context context, final int id, BMessage message, final int smallIconResID, final Uri soundUri, final int number){
        if (DEBUG) Timber.v("createMessageNotification");

        final Intent resultIntent = getChatResultIntent(context);
        resultIntent.putExtra(ChatSDKChatActivity.THREAD_ID,  message.getThreadDaoId());
        resultIntent.putExtra(ChatSDKChatActivity.FROM_PUSH, true);
        resultIntent.putExtra(ChatSDKChatActivity.MSG_TIMESTAMP, message.getDate().getTime());

        String msgContent = message.getType() == TEXT ? message.getText() : message.getType() == IMAGE ? context.getString(R.string.not_image_message) : context.getString(R.string.not_location_message);

        String title = !StringUtils.isEmpty(
                message.getBUserSender().getMetaName()) ? message.getBUserSender().getMetaName() : " ";

        final Bundle data = NotificationUtils.getDataBundle(title, "New message from " + message.getBUserSender().getMetaName(), msgContent);

        getNotificationLines(context, message, data);
        
        Bitmap threadImage = null;
        if (message.getThread() != null)
        {
            final String urls[] = message.getThread().threadImageUrl().split(",");

            if (urls.length > 1)
            {
                threadImage = VolleyUtils.getBitmapCache().getBitmap(MakeThreadImage.getCacheKey(message.getThread().getEntityID(), urls.length));
            }
            else if (urls.length == 1)
            {
                threadImage = VolleyUtils.getBitmapCache().getBitmap(VolleyUtils.BitmapCache.getCacheKey(urls[0]));
            }
            
            // Trying to load the sender image.
            if (threadImage == null && message.getBUserSender() != null
                    && StringUtils.isNotEmpty(message.getBUserSender().getMetaPictureUrl()))
            {
                VolleyUtils.getImageLoader().get(message.getBUserSender().getMetaPictureUrl(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        if (imageContainer.getBitmap() != null)
                            createAlertNotification(context, id, resultIntent, data, imageContainer.getBitmap(), smallIconResID, soundUri, number);
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        createAlertNotification(context, id, resultIntent, data, null, smallIconResID, soundUri, number);
                    }
                });
                
                return;
            }

            createAlertNotification(context, id, resultIntent, data,threadImage, smallIconResID, soundUri, number);
        }
        
        
    }
    
 
    private static String getMessageContent(Context context, BMessage message){
        return String.format("%s: %s",
                message.getBUserSender().getMetaName(),
                message.getType() == TEXT ? message.getText()
                : message.getType() == IMAGE ? context.getString(R.string.not_image_message)
                : context.getString(R.string.not_location_message));
    }
 
    private static ArrayList<String> getNotificationLines(Context context, BMessage message, Bundle data){
        List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getThreads(BThread.Type.Private);

        if (DEBUG) Timber.v("getNotification, Thread size: %s", threads == null ? "0" : threads.size());

        if (threads == null)
            return new ArrayList<>();

        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> senders = new ArrayList<>();
        
        int linesCount = 0;
        List<BMessage> m;

        // Getting the lines to use for this message notification
        // A max of three lines could be added from each thread.
        // There is also a max amount of lines to use defined in BDefines.MaxInboxNotificationLines.
        for (BThread t : threads)
        {
            m = t.getMessagesWithOrder(DaoCore.ORDER_DESC);


            if (DEBUG) Timber.v("getNotification, Thread messages size: %s", m.size());

            // Max of three lines from each thread.
            for (int i = 0 ; i < 3; i++){
                if ( validateLinesAndMessagesSize(m, i, lines) )
                {
                    addLine(context, m.get(i), lines, senders);
                }
                else break;
            }
            
            // Checking to see that we are still under the max amount of lines to use.
            if (linesCount >= BDefines.Options.MaxInboxNotificationLines)
                break;
        }

        // Creating the title for the notification
        if (senders.size() > 1)
        {
            data.putString(TITLE, StringUtils.join(senders, ", "));
        }
        
        // Adding the lines data
        if (lines.size() > 0)
        {
            data.putStringArrayList(LINES, lines);
            
            // Adding summary, Total amount of unread messages.
            if (lines.size() > 3)
            {
                data.putString(SUMMARY, String.format(context.getString(R.string.not_messages_summary), BNetworkManager.sharedManager().getNetworkAdapter().getUnreadMessagesAmount(false)));
            }
        }
        
        return lines;
    }
    
    private static boolean addLine(Context context, BMessage message, ArrayList<String> lines, ArrayList<String> senders){

        if(message != null && !message.wasRead())
        {
            lines.add(getMessageContent(context, message));

            String senderName = message.getBUserSender().getMetaName();
            if (!senders.contains(senderName))
                senders.add(senderName);

            return true;
        }
        else if (DEBUG)
            Timber.i("addLine, message was read? %s, payload: %s",
                    message == null? "message is null" : message.wasRead(), message == null ? "null" : message.getText());
        
        return false;
    }
  
    private static boolean validateLinesAndMessagesSize(List<BMessage> m, int minMessagesSize, ArrayList<String> lines){
        return m.size() > minMessagesSize && lines.size() < BDefines.Options.MaxInboxNotificationLines;
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
