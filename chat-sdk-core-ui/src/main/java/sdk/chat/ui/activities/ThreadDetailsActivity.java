/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.core.utils.Strings;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.fragments.ThreadUsersFragment;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.chat.ui.utils.ToastHelper;

/**
 * Created by Ben Smiley on 24/11/14.
 */
public class ThreadDetailsActivity extends ImagePreviewActivity {

    protected Thread thread;

    protected ThreadUsersFragment usersFragment;

    protected ActionBar actionBar;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;
    @BindView(R2.id.threadUsersFrame) protected FrameLayout threadUsersFrame;
//    @BindView(R2.id.titleTextView) protected TextView titleTextView;

    @BindView(R2.id.addUsersFab) protected FloatingActionButton addUsersFab;
    @BindView(R2.id.refreshFab) protected FloatingActionButton refreshFab;
    @BindView(R2.id.headerImageView) protected ImageView headerImageView;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_thread_details;
    }

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
//        if (thread.typeIs(ThreadType.Private1to1)) {
//            titleTextView.setVisibility(View.INVISIBLE);
//        } else {
//            titleTextView.setVisibility(View.VISIBLE);
//        }

        if (ChatSDK.thread().canAddUsersToThread(thread)) {
            addUsersFab.setImageDrawable(Icons.get(this, Icons.choose().add, R.color.white));
            addUsersFab.setOnClickListener(v -> {
                addUsersFab.setEnabled(false);
                ChatSDK.ui().startAddUsersToThreadActivity(this, thread.getEntityID());
            });
        } else {
            addUsersFab.setVisibility(View.INVISIBLE);
        }

        if (ChatSDK.thread().canRefreshRoles(thread)) {
            refreshFab.setImageDrawable(Icons.get(this, Icons.choose().refresh, R.color.white));
            refreshFab.setOnClickListener(v -> {
                ChatSDK.thread().refreshRoles(thread).subscribe();
            });
        } else {
            refreshFab.setVisibility(View.INVISIBLE);
        }

        int profileHeader = UIModule.config().profileHeaderImage;
        headerImageView.setImageResource(profileHeader);

        ChatSDK.thread().refreshRoles(thread).subscribe();
    }

    protected void initViews() {
        super.initViews();

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> reloadData(), this));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterRoleUpdated(thread))
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

//        titleTextView.setText(name);

        if (!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
            avatarImageView.setOnClickListener(v -> zoomImageFromThumbnail(avatarImageView, thread.getImageUrl()));
            Glide.with(this).load(thread.getImageUrl()).dontAnimate().into(avatarImageView);
        } else {
            ThreadImageBuilder.load(avatarImageView, thread, Dimen.from(this, R.dimen.large_avatar_width));
            avatarImageView.setOnClickListener(null);
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
        if (ChatSDK.thread().canRefreshRoles(thread)) {
            ChatSDK.thread().refreshRoles(thread).subscribe();
        }
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
        if (!ChatSDK.thread().canEditThreadDetails(thread)) {
            menu.removeItem(R.id.action_edit);
        }

        if (!ChatSDK.thread().muteEnabled(thread)) {
            menu.removeItem(R.id.action_mute);
        }

        if (!ChatSDK.thread().canLeaveThread(thread)) {
            menu.removeItem(R.id.action_leave);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_edit) {
            ChatSDK.ui().startEditThreadActivity(this, thread.getEntityID());
        }
        if (item.getItemId() == R.id.action_mute) {
            if (thread.isMuted()) {
                ChatSDK.thread().unmute(thread).subscribe(this);
            } else {
                ChatSDK.thread().mute(thread).subscribe(this);
            }
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.action_leave) {
            ChatSDK.thread().leaveThread(thread).doOnComplete(this::finish).subscribe(this);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_mute);

        if (item != null) {
            String muteText = getApplicationContext().getString(R.string.mute_notifications);
            String unmuteText = getApplicationContext().getString(R.string.unmute_notifications);

            if (thread.isMuted()) {
                item.setTitle(unmuteText);
            } else {
                item.setTitle(muteText);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

}
