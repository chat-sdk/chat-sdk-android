/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Bundle;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.fragments.ChatSDKContactsFragment;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by braunster on 27/07/14.
 */
public class ChatSDKShareWithContactsActivity extends ChatSDKBaseActivity {

    public static final String TAG = ChatSDKShareWithContactsActivity.class.getSimpleName();
    public static final boolean DEBUG = Debug.ShareWithContactsActivity;

    /*FIXME add a check to see if there is any user logged in, If there is not add an option for a quick login just for passing all the data.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_fragment_activity);

        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (StringUtils.isEmpty(action) && StringUtils.isEmpty(type))
        {
            showAlertToast(getString(R.string.share_activity_error_getting_share_data));
            return;
        }

        if (!action.equals(Intent.ACTION_SEND))
        {
            showAlertToast(getString(R.string.share_activity_error_getting_share_data));
            return;
        }

        if (getIntent().getExtras() != null)
        {
            Object extraData = null;

            if (type.equals("text/plain"))
            {
                extraData = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            }
            else if (type.startsWith("image/"))
            {
                extraData = getIntent().getExtras().get(Intent.EXTRA_STREAM);
            }

            ChatSDKContactsFragment fragment = ChatSDKContactsFragment.newInstance(ChatSDKContactsFragment.MODE_LOAD_CONTACTS, ChatSDKContactsFragment.CLICK_MODE_SHARE_CONTENT, extraData);
            getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        }
        else showAlertToast(getString(R.string.share_activity_error_getting_share_data));
    }
}
