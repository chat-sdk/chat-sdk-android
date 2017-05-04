/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.ActivityTemplates;

import android.os.Bundle;
import android.widget.AbsListView;

import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;


/**
 * Created by itzik on 6/8/2014.
 */
public class ChatSDKChatActivity extends ChatSDKAbstractChatActivity implements AbsListView.OnScrollListener{

    private static final String TAG = ChatSDKChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_sdk_activity_chat);

        initViews();

        chatSDKChatHelper.checkIfWantToShare(getIntent());
    }
}
