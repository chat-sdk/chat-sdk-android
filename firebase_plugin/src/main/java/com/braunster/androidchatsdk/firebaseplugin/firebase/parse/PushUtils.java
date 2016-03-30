package com.braunster.androidchatsdk.firebaseplugin.firebase.parse;

import android.util.Log;

import com.braunster.chatsdk.dao.BMessage;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;

/**
 * Created by itzik on 6/3/2014.
 */
public class PushUtils {

    private static final String TAG = PushUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String ACTION = "action";
    public static final String ALERT = "alert";
    public static final String BADGE = "badge", INCREMENT = "Increment";
    public static final String CONTENT = "text";
    public static final String MESSAGE_ENTITY_ID = "message_entity_id";
    public static final String THREAD_ENTITY_ID = "thread_entity_id";
    public static final String MESSAGE_DATE ="message_date";
    public static final String MESSAGE_SENDER_ENTITY_ID ="message_sender_entity_id";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String MESSAGE_PAYLOAD= "message_payload";

    public static void sendMessage(BMessage message, Collection<String> channels){

        String text = message.getText();

        if (message.getType() == LOCATION)
            text = "Location Message";
        else if (message.getType() == IMAGE)
            text = "Picture Message";
        text = message.getBUserSender().getMetaName() + " " + text;

        if (DEBUG) Log.v(TAG, "sendMessage, Content: " +  text);

        JSONObject data = new JSONObject();
        try {

            data.put(ACTION, ChatSDKReceiver.ACTION_MESSAGE);

            data.put(CONTENT, text);
            data.put(MESSAGE_ENTITY_ID, message.getEntityID());
            data.put(THREAD_ENTITY_ID, message.getBThreadOwner().getEntityID());
            data.put(MESSAGE_DATE, message.getDate().getTime());
            data.put(MESSAGE_SENDER_ENTITY_ID, message.getBUserSender().getEntityID());
            data.put(MESSAGE_TYPE, message.getType());
            data.put(MESSAGE_PAYLOAD, message.getText());

            //For iOS
            data.put(BADGE, INCREMENT);
//            data.put(ALERT, text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);
        push.setChannels(channels);
        push.setData(data);
        push.sendInBackground();
    }

    /** @param channel The channel to push to.
     * @param content The follow notification content.*/
    public static void sendFollowPush(String channel, String content){
        JSONObject data = new JSONObject();
        try {

            data.put(ACTION, ChatSDKReceiver.ACTION_FOLLOWER_ADDED);
            data.put(CONTENT, content);

            //For iOS
            data.put(BADGE, INCREMENT);
//            data.put(ALERT, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);
        push.setChannel(channel);
        push.setData(data);
        push.sendInBackground();
    }
}
