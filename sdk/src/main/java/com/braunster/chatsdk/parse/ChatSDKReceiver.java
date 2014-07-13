package com.braunster.chatsdk.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.activities.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by braunster on 09/07/14.
 */
public class ChatSDKReceiver extends BroadcastReceiver {

    private static final String TAG = ChatSDKReceiver.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String MESSAGE_ACTION = "com.braunster.chatsdk.parse.MESSAGE_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (DEBUG) Log.v(TAG, "onReceive");

            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            if (DEBUG) Log.d(TAG, "got action " + action + " on channel " + channel + " with:");

            Iterator itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }

            Intent resultIntent = new Intent(context, LoginActivity.class);

            NotificationUtils.createAlertNotification(context, PushUtils.MESSAGE_NOTIFICATION_ID, resultIntent,
                    NotificationUtils.getDataBundle("Message", "You got new message", json.getString(PushUtils.CONTENT)));

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}
