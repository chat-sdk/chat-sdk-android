/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BThreadDao;
import com.braunster.chatsdk.dao.core.DaoCore;

import org.apache.commons.lang3.StringUtils;

import timber.log.Timber;

/**
 * Created by braunster on 24/11/14.
 */
public class ChatSDKBaseThreadActivity extends ChatSDKBaseActivity {

    private static final String TAG = ChatSDKBaseThreadActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.ChatSDKBaseThreadActivity;


    /** The key to get the thread long id.*/
    public static final String THREAD_ID = "thread_id";
    public static final String THREAD_ENTITY_ID = "Thread_Entity_ID";

    public static final String MODE = "mode";

    public static final int MODE_NONE = -1991;

    /** Set true if you want slide down animation for this activity exit. */
    protected boolean animateExit = false;

    /** Default value - MODE_NEW_CONVERSATION*/
    protected int mode = MODE_NONE;

    /** For add to conversation mode.*/
    protected long threadID = -1;

    protected String threadEntityId = "";

    protected BThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            getDataFromBundle(savedInstanceState);
        }
        else
        {
            if (getIntent().getExtras() != null)
            {
                getDataFromBundle(getIntent().getExtras());
            }
            else
                finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBundle(intent.getExtras());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (animateExit)
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
    }

    private void getDataFromBundle(Bundle bundle){
        if (bundle == null)
        {
            return;
        }

        mode = bundle.getInt(MODE, mode);
        threadID = bundle.getLong(THREAD_ID, threadID);
        threadEntityId = bundle.getString(THREAD_ENTITY_ID, threadEntityId);
        animateExit = bundle.getBoolean(ChatSDKAbstractChatActivity.ANIMATE_EXIT, animateExit);

        if (threadID != -1)
        {
            thread = DaoCore.fetchEntityWithProperty(BThread.class,
                    BThreadDao.Properties.Id,
                    threadID);
        }
        else  if (StringUtils.isNotBlank(threadEntityId))
        {
            thread = DaoCore.fetchEntityWithProperty(BThread.class,
                    BThreadDao.Properties.EntityID,
                    threadEntityId);
        }

        if (thread == null)
        {
            if (DEBUG) Timber.e("Thread is null");
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(THREAD_ID, threadID);
        outState.putString(THREAD_ENTITY_ID, threadEntityId);
        outState.putInt(MODE, mode);
        outState.putBoolean(ChatSDKAbstractChatActivity.ANIMATE_EXIT, animateExit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

}
