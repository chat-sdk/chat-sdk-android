package com.braunster.chatsdk.Utils.helper;

import android.content.Intent;
import android.os.Bundle;

import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;

/**
 * This class is used in the ChatSDKMainActivity for detecting if the activity was open or got new intent
 * from a notification message in the notification drawer.
 *
 * One check is made in the main activity {@link android.app.Activity#onCreate(android.os.Bundle)}  onCreate} 
 * 
 * another is made in the activity {@link android.app.Activity#onNewIntent(android.content.Intent)}  onNewIntent}
 *
 * Also we need to call the {@link android.app.Activity#onSaveInstanceState(android.os.Bundle) onSaveInstanceState} 
 * so the value of the timestamp will be kept during activity life cycles changes.
 *
 * * Created by braunster on 06/02/15.
 */
public class OpenFromPushChecker {


    private static final String LAST_MSG_TIMESTAMP = "last_open_msg_timestamp";
    
    /**
     * The last incoming message timestamp
     **/
    private long msgTimestamp = 0;

    /**
     * @return  true if the intent is from a valid(using timestamp) push message.
     * * * */
    public boolean checkOnCreate(Intent intent, Bundle savedInstanceState){
        if (intent.getExtras()!= null && intent.getExtras().getBoolean(ChatSDKChatActivity.FROM_PUSH, false))
        {
            msgTimestamp = intent.getExtras().getLong(ChatSDKAbstractChatActivity.MSG_TIMESTAMP);

            // We are checking to see if this message was not opened before.
            if (savedInstanceState == null || msgTimestamp > savedInstanceState.getLong(LAST_MSG_TIMESTAMP, -1))
            {
                intent.getExtras().remove(ChatSDKAbstractChatActivity.FROM_PUSH);
                return true;
            }

            return false;
        }
        
        return false;
    }
    
    /**
     * @return  true if the intent is from a push message.
     * * * */
    public boolean checkOnNewIntent(Intent intent){
        if (intent.getExtras()!= null && intent.getExtras().getBoolean(ChatSDKChatActivity.FROM_PUSH, false))
        {
            intent.getExtras().remove(ChatSDKAbstractChatActivity.FROM_PUSH);

            return true;
        }
        
        return false;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(LAST_MSG_TIMESTAMP, msgTimestamp);
    }
}
