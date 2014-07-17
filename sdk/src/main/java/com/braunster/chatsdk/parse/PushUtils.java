package com.braunster.chatsdk.parse;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

/**
 * Created by itzik on 6/3/2014.
 */
public class PushUtils {

    private static final String TAG = PushUtils.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String ACTION = "action";
    public static final String ALERT = "alert";
    public static final String CONTENT = "text";

    public static final int MESSAGE_NOTIFICATION_ID = 1000;

    public static void sendMessage(String content, String channel){
        if (DEBUG) Log.v(TAG, "sendMessage, Content: " +  content + ", Channel: " + channel);

        JSONObject data = new JSONObject();
        try {
            data.put(ACTION, ChatSDKReceiver.MESSAGE_ACTION);
            data.put(CONTENT, content);
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

    public static void sendMessage(String content, Collection<String> channels){
        if (DEBUG) Log.v(TAG, "sendMessage, Content: " +  content);

        JSONObject data = new JSONObject();
        try {
            data.put(ACTION, ChatSDKReceiver.MESSAGE_ACTION);
            data.put(CONTENT, content);
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

    public static void push(String content, String channel){

        JSONObject data = new JSONObject();
        try {
            data.put(ACTION, ChatSDKReceiver.MESSAGE_ACTION);
            data.put(CONTENT, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
        ParsePush push = new ParsePush();
        push.setQuery(parseQuery);
        push.setChannel(channel);
        push.setMessage(content);
        try {
            push.send();
            // sendInTheBackground has high delay send works better for now.
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
