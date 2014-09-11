package com.braunster.chatsdk.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.abstracted.AbstractChatActivity;


/**
 * Created by itzik on 6/8/2014.
 */
public class ChatSDKChatActivity extends AbstractChatActivity implements View.OnKeyListener, View.OnClickListener, TextView.OnEditorActionListener, AbsListView.OnScrollListener{

    private static final String TAG = ChatSDKChatActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.v(TAG, "onCreate");

        setContentView(R.layout.chat_sdk_activity_chat);

        initViews();

        initToast();
    }
}
