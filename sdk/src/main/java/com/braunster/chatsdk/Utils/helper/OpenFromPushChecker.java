/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.helper;

import android.content.Intent;
import android.os.Bundle;

import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;

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
