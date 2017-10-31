/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.session.NM;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.contacts.ContactsFragment;
import co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.core.utils.Strings;
import io.reactivex.functions.Consumer;

/**
 * Created by braunster on 24/11/14.
 */
public class ThreadDetailsActivity extends BaseActivity {

    /** Set true if you want slide down animation for this context exit. */
    protected boolean animateExit = false;

    protected Thread thread;
    private SimpleDraweeView threadImageView;

    private ContactsFragment contactsFragment;
    private DisposableList disposableList = new DisposableList();

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        }
        else {
            if (getIntent().getExtras() != null) {
                getDataFromBundle(getIntent().getExtras());
            }
            else {
                finish();
            }
        }

        setContentView(R.layout.chat_sdk_activity_thread_details);

        initViews();

        disposableList.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.threadUsersUpdated())
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                        loadData();
                    }
                }));

        loadData();
    }

    private void initViews() {

        actionBar = getSupportActionBar();
        actionBar.setTitle(Strings.nameForThread(thread));
        actionBar.setHomeButtonEnabled(true);

        final View actionBarView = getLayoutInflater().inflate(R.layout.chat_sdk_activity_thread_details, null);

        // Allow the thread name to be modified by a long click
        actionBarView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO: Implement this
                return true;
            }
        });

        threadImageView = (SimpleDraweeView) findViewById(R.id.chat_sdk_thread_image_view);
    }

    private void loadData () {

        ThreadImageBuilder.load(threadImageView, thread);

        // CoreThread users bundle
        contactsFragment = new ContactsFragment();
        contactsFragment.setInflateMenu(false);
        contactsFragment.setLoadingMode(ContactsFragment.MODE_LOAD_THREAD_USERS);
        contactsFragment.setExtraData(thread.getEntityID());
        contactsFragment.setClickMode(ContactsFragment.CLICK_MODE_NONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_thread_users, contactsFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only if the current user is the admin of this thread.
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(NM.currentUser().getEntityID())) {
            //threadImageView.setOnClickListener(ChatSDKIntentClickListener.getPickImageClickListener(this, THREAD_PIC));
            threadImageView.setOnClickListener(new ProfilePictureChooserOnClickListener(this));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(AppCompatActivity.RESULT_OK);

        finish();
        if (animateExit) {
            overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO: Enable thread images
    }

    @Override
    protected void onStop() {
        disposableList.dispose();
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBundle(intent.getExtras());
    }

    private void getDataFromBundle(Bundle bundle){
        if (bundle == null) {
            return;
        }

        animateExit = bundle.getBoolean(ChatActivity.ANIMATE_EXIT, animateExit);

        String threadEntityID = bundle.getString(BaseInterfaceAdapter.THREAD_ENTITY_ID);

        if(threadEntityID != null && threadEntityID.length() > 0) {
            thread = StorageManager.shared().fetchThreadWithEntityID(threadEntityID);
        }
        else {
            finish();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BaseInterfaceAdapter.THREAD_ENTITY_ID, thread.getEntityID());
        outState.putBoolean(ChatActivity.ANIMATE_EXIT, animateExit);
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
