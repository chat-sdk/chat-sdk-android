package com.braunster.chatsdk.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.braunster.chatsdk.Utils.ChatSDKUiHelper;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by braunster on 09/07/14.
 *
 * The receiver is the sole object to handle push notification from parse server.
 *
 * The receiver will only notify for the currentUser() incoming messages any message for other user will be <b>ignored</b>.
 * This behavior is due to multiple connection from the same phone.
 *
 * Then the receiver will check to see if the message is already on the db, if the message exist it will be <b>ignored</b>.
 * This behavior prevents notifying the user for a message that is already seen by the user while the push was on its way.
 *
 * Then the receiver will parse the message data from the push json. After that it will validate, build and save the message to the db.
 *
 * Then the receiver will check if the user is authenticated, If he his the notification will lead him to the ChatActivity else he will be directed to the LoginActivity.
 *
 */
public class ChatSDKReceiver extends BroadcastReceiver {

    private static final String TAG = ChatSDKReceiver.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String MESSAGE_ACTION = "com.braunster.chatsdk.parse.MESSAGE_RECEIVED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            if (DEBUG) Log.v(TAG, "onReceive");

            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            final JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            if (DEBUG) Log.d(TAG, "got action " + action + " on channel " + channel + " with:");

            // If the push is not for the current user we ignore it.
            if (BNetworkManager.sharedManager().getNetworkAdapter() != null) {
                BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
                if (user != null && !channel.equals(user.getPushChannel()))
                    return;
            }

/*            Iterator itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }*/

            // Extracting the message data from the push json.
            String entityID = json.getString(PushUtils.MESSAGE_ENTITY_ID);
            final String threadEntityID = json.getString(PushUtils.THREAD_ENTITY_ID);
            final Long dateLong =json.getLong(PushUtils.MESSAGE_DATE);
            final Date date = new Date(dateLong);
            final String senderEntityId = json.getString(PushUtils.MESSAGE_SENDER_ENTITY_ID);
            final Integer type = json.getInt(PushUtils.MESSAGE_TYPE);
            final String messagePayload = (json.getString(PushUtils.MESSAGE_PAYLOAD));

            if (DEBUG) Log.d(TAG, "Pushed message entity id: " + entityID);
            if (DEBUG) Log.d(TAG, "Pushed message thread entity id: " + threadEntityID);

            BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, entityID);

            if (message != null)
            {
                Log.d(TAG, "Message already exist");
                return;
            }

            message = new BMessage();

            message.setDate(date);
            message.setType(type);
            message.setText(messagePayload);
            message.setEntityID(entityID);

            BUser sender = DaoCore.fetchEntityWithEntityID(BUser.class, senderEntityId);
            BThread thread =DaoCore.fetchEntityWithEntityID(BThread.class, threadEntityID);

            boolean check = true;
            if (sender != null && thread != null)
            {
                message.setBUserSender(sender);
                message.setBThreadOwner(thread);
                message = DaoCore.createEntity(message);
            } else check = false;

            final boolean messageIsValid = check;

            // Checking to see if the user is authenticated so we can decide to where we should direct it from the notification.
            Firebase ref = FirebasePaths.firebaseRef();
            final SimpleLogin simpleLogin = new SimpleLogin(ref, context);
            simpleLogin.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
                @Override
                public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                    Intent resultIntent;
                    if (error == null && user != null)
                    {
                        // If the message is valide(Sender and Thread exist in the db) we should lead the user to the chat.
                        if (messageIsValid)
                        {
                            resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().chatActivity);
                            resultIntent.putExtra(ChatSDKChatActivity.THREAD_ENTITY_ID, threadEntityID);
                            resultIntent.putExtra(ChatSDKChatActivity.FROM_PUSH, true);
                        }
                        // Open main activity
                        else resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().mainActivity);

                    }
                    // Id user isn't authenticated we should open login so he could auth himself in.
                    else {
                        resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().loginActivity);
                    }

                    // Posting the notification.
                    try {
                        NotificationUtils.createAlertNotification(context, null, PushUtils.MESSAGE_NOTIFICATION_ID, resultIntent,
                                NotificationUtils.getDataBundle("Message", "You got new message", json.getString(PushUtils.CONTENT)));
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: " + e.getMessage());
                    }
                }
            });


        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}
