/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;


import butterknife.BindView;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.fragments.ThreadUsersFragment;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import co.chatsdk.ui.utils.ToastHelper;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ben Smiley on 24/11/14.
 */
public class ThreadDetailsActivity extends ImagePreviewActivity {

    protected Thread thread;

    protected ThreadUsersFragment usersFragment;

    protected ActionBar actionBar;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.threadImageView) protected CircleImageView threadImageView;
    @BindView(R2.id.threadUsersFrame) protected FrameLayout threadUsersFrame;
    @BindView(R2.id.nameTextView) protected TextView nameTextView;
    @BindView(R2.id.root) protected ScrollView root;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        } else {
            if (getIntent().getExtras() != null) {
                getDataFromBundle(getIntent().getExtras());
            } else {
                finish();
            }
        }
        if (thread == null) {
            ToastHelper.show(this, R.string.error_thread_not_found);
            finish();
        }

        initViews();

        // Depending on the thread type, disable / enable options
        if (thread.typeIs(ThreadType.Private1to1)) {
            nameTextView.setVisibility(View.INVISIBLE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
        }

    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_thread_details;
    }

    protected void initViews() {
        super.initViews();

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.threadDetailsUpdated())
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> reloadData(), this));

        reloadData();
    }

    protected void reloadData() {
        actionBar = getSupportActionBar();
        String name = Strings.nameForThread(thread);
        if (actionBar != null) {
            actionBar.setTitle(name);
            actionBar.setHomeButtonEnabled(true);
        }
        nameTextView.setText(name);

        if (!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
            threadImageView.setOnClickListener(v -> zoomImageFromThumbnail(threadImageView, thread.getImageUrl()));
            Glide.with(this).load(thread.getImageUrl()).dontAnimate().into(threadImageView);
        } else {
            ThreadImageBuilder.load(threadImageView, thread);
            threadImageView.setOnClickListener(null);
        }

        // CoreThread users bundle
        if (usersFragment == null) {
            usersFragment = new ThreadUsersFragment(thread);
            getSupportFragmentManager().beginTransaction().replace(R.id.threadUsersFrame, usersFragment).commit();
        } else {
            usersFragment.loadData(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish(); // Finish needs to be called before animate exit
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO: Enable thread images
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBundle(intent.getExtras());
    }

    protected void getDataFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return;
        }

        String threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID);

        if (threadEntityID != null && !threadEntityID.isEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_thread_details_menu, menu);

        // Only the creator can modify the group. Also, private 1-to-1 chats can't be edited
        if (!thread.getCreator().isMe() || thread.typeIs(ThreadType.Private1to1)) {
            menu.removeItem(R.id.action_edit);
        }

        if (!ChatSDK.thread().muteEnabled(thread)) {
            menu.removeItem(R.id.action_mute);
        }

        if (!ChatSDK.thread().addUsersEnabled(thread)) {
            menu.removeItem(R.id.action_add_users);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_edit) {
            ChatSDK.ui().startEditThreadActivity(ChatSDK.shared().context(), thread.getEntityID());
        }
        if (item.getItemId() == R.id.action_mute) {
            if (thread.isMuted()) {
                ChatSDK.thread().unmute(thread).subscribe(this);
            } else {
                ChatSDK.thread().mute(thread).subscribe(this);
            }
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.action_add_users) {
            ChatSDK.ui().startAddUsersToThreadActivity(this, thread.getEntityID());
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_mute);

        if (item != null) {
            String muteText = getApplicationContext().getString(R.string.mute_notifications);
            String unmuteText = getApplicationContext().getString(R.string.unmute_notifications);

            if (thread.metaValueForKey(Keys.Mute) != null) {
                item.setTitle(unmuteText);
            } else {
                item.setTitle(muteText);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

}
