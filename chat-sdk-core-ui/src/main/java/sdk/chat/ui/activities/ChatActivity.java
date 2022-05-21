package sdk.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.fragments.AbstractChatFragment;
import sdk.chat.ui.fragments.ChatFragment;
import sdk.chat.ui.module.UIModule;

public class ChatActivity extends BaseActivity implements ChatFragment.Delegate {

    protected Thread thread;
    protected AbstractChatFragment chatFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateThread(savedInstanceState);
        initViews();

        chatFragment = ChatSDKUI.getChatFragment(thread, this);

        getSupportFragmentManager().beginTransaction().add(R.id.chatViewWrapper, chatFragment).commit();

    }

    public void updateThread(Bundle bundle) {
        thread = null;

        if (bundle == null) {
            bundle = getIntent().getExtras();
        }

        if (bundle != null && bundle.containsKey(Keys.IntentKeyThreadEntityID)) {
            String threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID);
            if (threadEntityID != null) {
                thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            }
        }

        if (thread == null) {
            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the thread ID
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        updateThread(intent.getExtras());

        chatFragment.onNewIntent(thread);

    }

    @Override
    public void onBackPressed() {
        if (!chatFragment.onBackPressed()) {
            if (UIModule.config().goToMainActivityOnChatActivityBackPressed) {
                // Do this so that even if we were editing the thread, we always go back to the
                // main activity
                ChatSDK.ui().startMainActivity(this);
            } else {
                super.onBackPressed();
            }
        }
    }
}
