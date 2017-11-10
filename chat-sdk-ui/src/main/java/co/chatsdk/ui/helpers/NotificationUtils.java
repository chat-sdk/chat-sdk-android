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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.R;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.utils.ImageBuilder;
import co.chatsdk.core.utils.Strings;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;

public class NotificationUtils {

    @Deprecated
    public static final int MESSAGE_NOTIFICATION_ID = 1001;

    private static final String TAG = NotificationUtils.class.getSimpleName();

    public static final String TITLE = "title";
    public static final String TICKER = "ticker";
    public static final String CONTENT = "content";
    public static final String LINES = "lines";
    public static final String SUMMARY= "summary";

    private static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data, int smallIconResID, Uri soundUri, int number){
       createAlertNotification(context, id, resultIntent, data, null, smallIconResID, soundUri, number);
    }

    private static void createAlertNotification(Context context, int id, Intent resultIntent, Bundle data, Bitmap bitmap, int smallIconResID, Uri soundUri, int number){
        String title, content;

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
                if (data.containsKey(LINES)) {
                    ArrayList<String> list = data.getStringArrayList(LINES);

                    if (list != null && list.size()>0) {
                        for (String s : list) {
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

    public static void createMessageNotification(Context context, Message message){
        createMessageNotification(context, message, R.drawable.ic_launcher, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), -1);
    }
    
    public static void createMessageNotification(Context context, Message message, int smallIconResID, Uri soundUri, int number){
        createMessageNotification(context, MESSAGE_NOTIFICATION_ID, message, smallIconResID, soundUri, number);
    }

    public static void createMessageNotification(final Context context, final int id, Message message, final int smallIconResID, final Uri soundUri, final int number){

        final Intent resultIntent = getChatResultIntent(context);
        resultIntent.putExtra(BaseInterfaceAdapter.THREAD_ENTITY_ID,  message.getThreadId());
        resultIntent.putExtra(Defines.FROM_PUSH, true);
        resultIntent.putExtra(Defines.MSG_TIMESTAMP, message.getDate().toDate().getTime());

        String messageContent = Strings.payloadAsString(message);

        String title = !StringUtils.isEmpty(
                message.getSender().getName()) ? message.getSender().getName() : " ";

        // TODO: Localize
        final Bundle data = NotificationUtils.getDataBundle(title, "New message from " + message.getSender().getName(), messageContent);

        getNotificationLines(context, data);

        ThreadImageBuilder.getImageUriForThread(context, message.getThread()).flatMap(new Function<Uri, SingleSource<Bitmap>>() {
            @Override
            public SingleSource<Bitmap> apply(@NonNull Uri uri) throws Exception {
                return ImageBuilder.bitmapForURL(context, uri.toString());
            }
        }).subscribe(new BiConsumer<Bitmap, Throwable>() {
            @Override
            public void accept(Bitmap bitmap, Throwable throwable) throws Exception {
                if(throwable == null) {
                    createAlertNotification(context, id, resultIntent, data, bitmap, smallIconResID, soundUri, number);
                }
                else {
                    createAlertNotification(context, id, resultIntent, data, null, smallIconResID, soundUri, number);
                }
            }
        });
    }
    
 
    private static String getMessageContent(Message message){
        return String.format("%s: %s",
                message.getSender().getName(),
                Strings.payloadAsString(message));
    }
 
    private static ArrayList<String> getNotificationLines(Context context, Bundle data){
        List<Thread> threads = NM.thread().getThreads(ThreadType.Private);

        if (threads == null)
            return new ArrayList<>();

        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> senders = new ArrayList<>();
        
        int linesCount = 0;
        List<Message> m;

        // Getting the lines to use for this message notification
        // A max of three lines could be added from each thread.
        // There is also a max amount of lines to use defined in Keys.MaxInboxNotificationLines.
        for (Thread t : threads)
        {
            m = t.getMessagesWithOrder(DaoCore.ORDER_DESC);

            // Max of three lines from each thread.
            for (int i = 0 ; i < 3; i++){
                if ( validateLinesAndMessagesSize(m, i, lines) )
                {
                    addLine(context, m.get(i), lines, senders);
                }
                else break;
            }
            
            // Checking to see that we are still under the max amount of lines to use.
            if (linesCount >= ChatSDK.config().maxInboxNotificationLines)
                break;
        }

        // Creating the title for the notification
        if (senders.size() > 1)
        {
            data.putString(TITLE, StringUtils.join(senders, ", "));
        }
        
        // Adding the lines bundle
        if (lines.size() > 0)
        {
            data.putStringArrayList(LINES, lines);
            
            // Adding summary, Total amount of unread messages.
            if (lines.size() > 3)
            {
                data.putString(SUMMARY, String.format(context.getString(R.string.not_messages_summary), NM.thread().getUnreadMessagesAmount(false)));
            }
        }
        
        return lines;
    }
    
    private static boolean addLine(Context context, Message message, ArrayList<String> lines, ArrayList<String> senders){

        if(message != null && !message.wasRead())
        {
            lines.add(getMessageContent(message));

            String senderName = message.getSender().getName();
            if (!senders.contains(senderName))
                senders.add(senderName);

            return true;
        }

        return false;
    }
  
    private static boolean validateLinesAndMessagesSize(List<Message> m, int minMessagesSize, ArrayList<String> lines){
        return m.size() > minMessagesSize && lines.size() < ChatSDK.config().maxInboxNotificationLines;
    }

    private static Intent getChatResultIntent(Context context){
        return new Intent(context, InterfaceManager.shared().a.getMainActivity());
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
