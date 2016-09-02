/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.backendless;

import android.content.Context;
import android.content.Intent;

import com.backendless.push.BackendlessBroadcastReceiver;
import com.braunster.androidchatsdk.firebaseplugin.R;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BThreadWrapper;
import com.braunster.androidchatsdk.firebaseplugin.firebase.wrappers.BUserWrapper;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.NotificationUtils;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.google.firebase.auth.FirebaseAuth;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import timber.log.Timber;

/**
 *
 * The receiver is the sole object to handle push notification from backendless server.
 *
 * The receiver will only notify for the currentUserModel() incoming messages any message for other user will be <b>ignored</b>.
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
public class ChatSDKReceiver extends BackendlessBroadcastReceiver {

    private static final String TAG = ChatSDKReceiver.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatSDKReceiver;

    public static final String ACTION_MESSAGE = "com.braunster.chatsdk.parse.MESSAGE_RECEIVED";
    public static final String ACTION_FOLLOWER_ADDED = "com.braunster.chatsdk.parse.FOLLOWER_ADDED";

    @Override
    public boolean onMessage(final Context context, Intent intent) {

        if (!BNetworkManager.preferences.getBoolean(BDefines.Prefs.PushEnabled, BNetworkManager.PushEnabledDefaultValue))
            return false;

        try {
            final JSONObject json = new JSONObject(intent.getExtras().getString("message"));

            String action = json.getString(BDefines.Keys.ACTION);

            if (action.equals(ACTION_MESSAGE))
            {
                // Getting the push channel used.
                String channel = json.getString(BDefines.Keys.Channel);

                if (DEBUG) Timber.d("got action: %s, on channel: %s ", action , channel);

                createMessageNotification(context, intent, channel);
            }
            // Follower added action
            else if (action.equals(ACTION_FOLLOWER_ADDED))
            {
                createFollowerNotification(context, intent);
            }
        } catch (JSONException e) {
            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
        }

        return false;
    }

    @SuppressWarnings("all")// For supressing the BMessasge setType(int type) warning.
    private void createMessageNotification(final Context context, Intent intent, String channel){
        if(DEBUG) Timber.v("receiver create message notification");
        try {
            if (DEBUG) Timber.v("onReceive");

            // The data saved for this push message.
            final JSONObject json = new JSONObject(intent.getExtras().getString("message"));

            // If the push is not for the current user we ignore it.
            if (BNetworkManager.sharedManager().getNetworkAdapter() != null) {
                BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();
                if (user != null && !channel.equals(user.getPushChannel()))
                    return;
            }

            // Extracting the message data from the push json.
            String entityID = json.getString(BDefines.Keys.MESSAGE_ENTITY_ID);
            final String threadEntityID = json.getString(BDefines.Keys.THREAD_ENTITY_ID);
            final String senderEntityId = json.getString(BDefines.Keys.MESSAGE_SENDER_ENTITY_ID);

            // Getting the sender and the thread.
            BUser sender = DaoCore.fetchEntityWithEntityID(BUser.class, senderEntityId);
            final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, threadEntityID);

            final Long dateLong =json.getLong(BDefines.Keys.MESSAGE_DATE);
            final Date date = new Date(dateLong);
            final Integer type = json.getInt(BDefines.Keys.MESSAGE_TYPE);
            final String messagePayload = (json.getString(BDefines.Keys.MESSAGE_PAYLOAD));

            if (DEBUG) Timber.d("Pushed message entity id: %s", entityID);
            if (DEBUG) Timber.d("Pushed message thread entity id: %s", threadEntityID);

            BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, entityID);

            // If the message isn't null that means the user already got notification for this message,
            // So we are ignoring it.
            if (message != null)
            {
                if (DEBUG) Timber.d("Message already exist");
                return;
            }

            // Creating the new message.
            message = new BMessage();

            message.setDate(date);
            message.setType(type);
            message.setText(messagePayload);
            message.setEntityID(entityID);
            message.setIsRead(false);

            // If we dont have the sender and thread we wont open the
            // chat activity when the notification is pressed.
            // Else we are setting the thread and sender to the message.
            if (sender != null && thread != null)
            {
                // Marking the thread as not deleted.
                thread.setDeleted(false);

                DaoCore.updateEntity(thread);

                message.setBUserSender(sender);
                message.setThread(thread);
                message = DaoCore.createEntity(message);

                postMessageNotification(context, json, thread, message, true);
            } else {

                if (DEBUG) Timber.d("Entity is null,Is null? Sender: %s, Thread: %s", sender== null, thread==null);

                // Getting the user and the thread from firebase
                final BMessage finalMessage = message;
                BUserWrapper.initWithEntityId(senderEntityId)
                        .once()
                        .then(new DoneCallback<BUser>() {
                            @Override
                            public void onDone(final BUser bUser) {
                                // Adding the user as the sender.
                                finalMessage.setBUserSender(bUser);

                                if (thread == null)
                                {
                                    final BThreadWrapper threadWrapper =
                                            new BThreadWrapper(threadEntityID);

                                    threadWrapper
                                            .on()
                                            .then(new DoneCallback<BThread>() {
                                                @Override
                                                public void onDone(BThread bThread) {

                                                    BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();
                                                    // Add the current user to the thread if needed.

                                                    if (!threadWrapper.getModel().hasUser(currentUser)) {
                                                        threadWrapper.addUser(BUserWrapper.initWithModel(currentUser));

                                                        // Connecting both users to the thread.
                                                        DaoCore.connectUserAndThread(currentUser, threadWrapper.getModel());
                                                        DaoCore.connectUserAndThread(bUser, threadWrapper.getModel());
                                                    }

                                                    // Adding the thread to the message.
                                                    finalMessage.setThread(bThread);

                                                    // posting the notification. Also creating the new updated message.
                                                    postMessageNotification(context, json, bThread, DaoCore.createEntity(finalMessage), true);

                                                    threadWrapper.off();
                                                }
                                            })
                                            .fail(new FailCallback() {
                                                @Override
                                                public void onFail(Object o) {
                                                    if (DEBUG) Timber.d("Failed to get thread.");
                                                    postMessageNotification(context, json, thread, finalMessage, false);

                                                    threadWrapper.off();
                                                }
                                            });
                                }
                                else postMessageNotification(context, json, thread, finalMessage, true);
                            }
                        }, new FailCallback<BError>() {
                            @Override
                            public void onFail(BError error) {
                                if (DEBUG) Timber.d("Failed to get user.");
                                postMessageNotification(context, json, thread, finalMessage, false);
                            }
                        });
            }
        } catch (JSONException e) {
            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
        }
    }

    private void postMessageNotification(Context context, JSONObject json, BThread thread, BMessage message, boolean messageIsValid){
        Timber.v("receiver postmessage notification");
        if (DEBUG) Timber.v("postMessageNotification: messageIsValid: %s", messageIsValid);

        Intent resultIntent;

        // If the user isn't authenticated press on the push will lead him to the
        // Login activity.
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            if (DEBUG) Timber.d("no auth user");
            resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().loginActivity);

            // Posting the notification.
            try {
                NotificationUtils.createAlertNotification(context, BDefines.MESSAGE_NOTIFICATION_ID, resultIntent,
                        NotificationUtils.getDataBundle(context.getString(R.string.not_message_title),
                                context.getString(R.string.not_message_ticker), json.getString(BDefines.Keys.CONTENT)));
            } catch (JSONException e) {
                if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
            }
        }
        else
        {
            if (DEBUG) Timber.i("user is authenticated");
            // If the message is valid(Sender and Thread exist in the db)
            // we should lead the user to the chat.
            if (messageIsValid)
            {
                NotificationUtils.createMessageNotification(context, message);
                return;
            }
            // Open main activity
            else resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().mainActivity);

            // Posting the notification.
            try {
                NotificationUtils.createAlertNotification(context, BDefines.MESSAGE_NOTIFICATION_ID, resultIntent,
                        NotificationUtils.getDataBundle(context.getString(R.string.not_message_title),
                                context.getString(R.string.not_message_ticker), json.getString(BDefines.Keys.CONTENT)));
            } catch (JSONException e) {
                if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
            }
        }
    }




    private void createFollowerNotification(Context context, Intent intent){
        if(DEBUG) Timber.v("receiver create follower notification");
        final JSONObject json;
        try {
            json = new JSONObject(intent.getExtras().getString("message"));
            Intent resultIntent = new Intent(context, ChatSDKUiHelper.getInstance().mainActivity);
            NotificationUtils.createAlertNotification(context, BDefines.FOLLOWER_NOTIFICATION_ID, resultIntent,
                    NotificationUtils.getDataBundle(context.getString(R.string.not_follower_title), context.getString(R.string.not_follower_ticker),
                            json.getString(BDefines.Keys.CONTENT)));
        } catch (JSONException e) {
            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
        }
    }
}
