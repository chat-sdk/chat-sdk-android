/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:35 PM
 */

package com.braunster.androidchatsdk.firebaseplugin.firebase.parse;

import com.braunster.chatsdk.dao.BMessage;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.IMAGE;
import static com.braunster.chatsdk.dao.entities.BMessageEntity.Type.LOCATION;

public class PushUtils {

    static final String ACTION = "action";
    static final String ALERT = "alert";
    static final String BADGE = "badge", INCREMENT = "Increment";
    static final String CONTENT = "text";
    static final String MESSAGE_ENTITY_ID = "message_entity_id";
    static final String THREAD_ENTITY_ID = "thread_entity_id";
    static final String MESSAGE_DATE ="message_date";
    static final String MESSAGE_SENDER_ENTITY_ID ="message_sender_entity_id";
    static final String MESSAGE_TYPE = "message_type";
    static final String MESSAGE_PAYLOAD= "message_payload";

    static final String SOUND = "sound";
    static final String Default = "default";

    static final String DeviceType = "deviceType";
    static final String iOS = "ios";
    static final String Android = "android";

    static final String Channels = "channels";
    static final String Channel = "channel";


    public static void sendMessage(BMessage message, Collection<String> channels){

        String text = message.getText();

        if (message.getType() == LOCATION)
            text = "Location Message";
        else if (message.getType() == IMAGE)
            text = "Picture Message";
        text = message.getSender().getName() + " " + text;

        JSONObject data = new JSONObject();
        try {

            data.put(ACTION, ChatSDKReceiver.ACTION_MESSAGE);

            data.put(CONTENT, text);
            data.put(MESSAGE_ENTITY_ID, message.getEntityID());
            data.put(THREAD_ENTITY_ID, message.getThread().getEntityID());
            data.put(MESSAGE_DATE, message.getDate().getTime());
            data.put(MESSAGE_SENDER_ENTITY_ID, message.getSender().getEntityID());
            data.put(MESSAGE_TYPE, message.getType());
            data.put(MESSAGE_PAYLOAD, message.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ParseQuery<ParseInstallation> androidQuery = ParseInstallation.getQuery();
        androidQuery.whereEqualTo(DeviceType, Android);
        androidQuery.whereContainedIn(Channels, channels);

        ParsePush androidPush = new ParsePush();
        androidPush.setQuery(androidQuery);
        androidPush.setData(data);
        androidPush.sendInBackground();

        //For iOS
        try {
            data.put(BADGE, INCREMENT);
            data.put(ALERT, text);
            // For making sound in iOS
            data.put(SOUND, Default);

            ParseQuery<ParseInstallation> iosQuery = ParseInstallation.getQuery();
            iosQuery.whereEqualTo(DeviceType, iOS);
            iosQuery.whereContainedIn(Channels, channels);

            ParsePush iOSPush = new ParsePush();
            iOSPush.setQuery(iosQuery);
            iOSPush.setData(data);
            iOSPush.sendInBackground();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }








    /** @param channel The channel to push to.
     * @param content The follow notification content.*/
    public static void sendFollowPush(String channel, String content){
        JSONObject data = new JSONObject();
        try {

            data.put(ACTION, ChatSDKReceiver.ACTION_FOLLOWER_ADDED);
            data.put(CONTENT, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseInstallation> androidQuery = ParseInstallation.getQuery();
        androidQuery.whereEqualTo(DeviceType, Android);
        androidQuery.whereEqualTo(Channel, channel);

        ParsePush androidPush = new ParsePush();
        androidPush.setQuery(androidQuery);
        androidPush.setData(data);
        androidPush.sendInBackground();


        //For iOS
        try {
            data.put(BADGE, INCREMENT);
            data.put(ALERT, content);
            // For making sound in iOS
            data.put(SOUND, Default);

            ParseQuery<ParseInstallation> iosQuery = ParseInstallation.getQuery();
            iosQuery.whereEqualTo(DeviceType, iOS);
            iosQuery.whereEqualTo(Channel, channel);

            ParsePush iOSPush = new ParsePush();
            iOSPush.setQuery(iosQuery);
            iOSPush.setData(data);
            iOSPush.sendInBackground();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
