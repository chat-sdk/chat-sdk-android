package com.braunster.chatsdk.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.BUserDao;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.ChatcatProfileFragment;

import timber.log.Timber;

/**
 * Created by braunster on 08.06.15.
 */
public class ChatSDKViewProfileActivity extends ChatSDKBaseActivity {

    public static final String USER_ID = "user_id";

    private BUser profileUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("onCreate");

        setContentView(R.layout.chat_sdk_activity_view_profile);

        Long userId = getIntent().getLongExtra(USER_ID, -1);

        if (userId == -1)
        {
            Timber.i("No user id passed to activity");
            finish();
            return;
        }

        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        profileUser = DaoCore.fetchEntityWithProperty(BUser.class, BUserDao.Properties.Id, userId);

        ChatcatProfileFragment chatcatProfileFragment;

        chatcatProfileFragment = (ChatcatProfileFragment) getFragmentManager().findFragmentById(R.id.frame_content);

        if (chatcatProfileFragment == null)
            chatcatProfileFragment= new ChatcatProfileFragment();

        chatcatProfileFragment.setProfileUser(profileUser);

        getFragmentManager().beginTransaction().replace(R.id.frame_content, chatcatProfileFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_view_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.action_message) {
            createAndOpenThreadWithUsers("", getNetworkAdapter().currentUserModel(), profileUser);

            return true;
        }
        else if (i == android.R.id.home)
        {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
