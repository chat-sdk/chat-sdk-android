/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package co.chatsdk.firebase.backendless;

import android.content.Context;
import android.content.Intent;

import com.backendless.push.BackendlessBroadcastReceiver;
import com.braunster.androidchatsdk.firebaseplugin.R;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.types.Defines;

import co.chatsdk.core.defines.Debug;
import co.chatsdk.core.dao.DaoCore;

import com.google.firebase.auth.FirebaseAuth;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import co.chatsdk.core.utils.AppContext;
import co.chatsdk.ui.helpers.UIHelper;
import timber.log.Timber;

import co.chatsdk.ui.helpers.NotificationUtils;

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
 * Then the receiver will parse the message bundle from the push json. After that it will validate, build and save the message to the db.
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

        if (!AppContext.getPreferences().getBoolean(co.chatsdk.core.types.Defines.Prefs.PushEnabled, false))
            return false;

        try {
            final JSONObject json = new JSONObject(intent.getExtras().getString("message"));

            String action = json.getString(DaoDefines.Keys.ACTION);

            if (action.equals(ACTION_MESSAGE))
            {
                // Getting the push channel used.
                String channel = json.getString(DaoDefines.Keys.Channel);

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

            // The bundle saved for this push message.
            final JSONObject json = new JSONObject(intent.getExtras().getString("message"));

            // If the push is not for the current user we ignore it.
            BUser user = NM.currentUser();
            if (user != null && !channel.equals(user.getPushChannel()))
                return;

            // Extracting the message bundle from the push json.
            String entityID = json.getString(DaoDefines.Keys.MESSAGE_ENTITY_ID);
            final String threadEntityID = json.getString(DaoDefines.Keys.THREAD_ENTITY_ID);
            final String senderEntityId = json.getString(DaoDefines.Keys.MESSAGE_SENDER_ENTITY_ID);

            // Getting the sender and the thread.
            BUser sender = DaoCore.fetchEntityWithEntityID(BUser.class, senderEntityId);
            final BThread thread = DaoCore.fetchEntityWithEntityID(BThread.class, threadEntityID);

            final Long dateLong = json.getLong(DaoDefines.Keys.MESSAGE_DATE);
            final DateTime date = new DateTime(dateLong);
            final Integer type = json.getInt(DaoDefines.Keys.MESSAGE_TYPE);
            final String messagePayload = (json.getString(DaoDefines.Keys.MESSAGE_PAYLOAD));

            if (DEBUG) Timber.d("Pushed message entity id: %s", entityID);
            if (DEBUG) Timber.d("Pushed message thread entity id: %s", threadEntityID);

            BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, entityID);

            // If the message isn't null that means the user already got notification for this message,
            // So we are ignoring it.
            if (message != null) {
                if (DEBUG) Timber.d("CoreMessage already exist");
                return;
            }

            // Creating the new message.
            message = new BMessage();

            message.setDate(date);
            message.setType(type);
            message.setTextString(messagePayload);
            message.setEntityID(entityID);
            message.setIsRead(false);

            // TODO: Look at this
//            postMessageNotification(context, json, thread, message, true);

        } catch (JSONException e) {
            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
        }

//
//            // If we dont have the sender and thread we wont open the
//            // chat activity when the notification is pressed.
//            // Else we are setting the thread and sender to the message.
//            if (sender != null && thread != null)
//            {
//                // Marking the thread as not deleted.
//                thread.setDeleted(false);
//
//                DaoCore.updateEntity(thread);
//
//                message.setSender(sender);
//                message.setThread(thread);
//                message = DaoCore.createEntity(message);
//
//                postMessageNotification(context, json, thread, message, true);
//            }
//            else {
//
//                if (DEBUG) Timber.d("CoreEntity is null,Is null? Sender: %s, CoreThread: %s", sender== null, thread==null);
//
//                // Getting the user and the thread from firebase
//                final BMessage finalMessage = message;
//
//                final UserWrapper userWrapper = UserWrapper.initWithEntityId(senderEntityId);
//
//                userWrapper.once().andThen(new CompletableSource() {
//                            @Override
//                            public void subscribe(CompletableObserver cs) {
//
//                                final BUser user = userWrapper.getModel();
//
//                                // Adding the user as the sender.
//                                finalMessage.setSender(user);
//
//                                if (thread == null)
//                                {
//                                    final ThreadWrapper threadWrapper = new ThreadWrapper(threadEntityID);
//
//                                    threadWrapper.once().doOnComplete(new Action() {
//                                        @Override
//                                        public void run() throws Exception {
//                                            BUser currentUser = NM.currentUser();
//                                            // Add the current user to the thread if needed.
//
//                                            if (!threadWrapper.getModel().hasUser(currentUser)) {
//                                                threadWrapper.addUser(UserWrapper.initWithModel(currentUser));
//
//                                                // Connecting both users to the thread.
//                                                DaoCore.connectUserAndThread(currentUser, threadWrapper.getModel());
//                                                DaoCore.connectUserAndThread(user, threadWrapper.getModel());
//                                            }
//
//                                            // Adding the thread to the message.
//                                            finalMessage.setThread(threadWrapper.getModel());
//
//                                            // posting the notification. Also creating the new updated message.
//                                            postMessageNotification(context, json, threadWrapper.getModel(), DaoCore.createEntity(finalMessage), true);
//
//                                            threadWrapper.off();
//                                        }
//                                    }).doOnError(new Consumer<Throwable>() {
//                                        @Override
//                                        public void accept(Throwable throwable) throws Exception {
//                                            if (DEBUG) Timber.d("Failed to get thread.");
//                                            postMessageNotification(context, json, thread, finalMessage, false);
//                                        }
//                                    }).subscribe();
//                                }
//                                else postMessageNotification(context, json, thread, finalMessage, true);
//                            }
//                        });
//           }
//        } catch (JSONException e) {
//            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
//        }
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
            resultIntent = new Intent(context, UIHelper.getInstance().getLoginActivity());

            // Posting the notification.
            try {
                NotificationUtils.createAlertNotification(context, Defines.MESSAGE_NOTIFICATION_ID, resultIntent,
                        NotificationUtils.getDataBundle(context.getString(R.string.not_message_title),
                                context.getString(R.string.not_message_ticker), json.getString(DaoDefines.Keys.CONTENT)));
            } catch (JSONException e) {
                if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
            }
        }
        else
        {
            if (DEBUG) Timber.i("user is authenticated");
            // If the message is valid(Sender and CoreThread exist in the db)
            // we should lead the user to the chat.
            if (messageIsValid)
            {
                NotificationUtils.createMessageNotification(context, message);
                return;
            }
            // Open main activity
            else resultIntent = new Intent(context, UIHelper.getInstance().getMainActivity());

            // Posting the notification.
            try {
                NotificationUtils.createAlertNotification(context, Defines.MESSAGE_NOTIFICATION_ID, resultIntent,
                        NotificationUtils.getDataBundle(context.getString(R.string.not_message_title),
                                context.getString(R.string.not_message_ticker), json.getString(DaoDefines.Keys.CONTENT)));
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
            Intent resultIntent = new Intent(context, UIHelper.getInstance().getMainActivity());
            NotificationUtils.createAlertNotification(context, Defines.FOLLOWER_NOTIFICATION_ID, resultIntent,
                    NotificationUtils.getDataBundle(context.getString(R.string.not_follower_title), context.getString(R.string.not_follower_ticker),
                            json.getString(DaoDefines.Keys.CONTENT)));
        } catch (JSONException e) {
            if (DEBUG) Timber.e(e.getCause(), "JSONException: %s", e.getMessage());
        }
    }
}
