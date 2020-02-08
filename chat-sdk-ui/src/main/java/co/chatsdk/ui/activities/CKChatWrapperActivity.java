package co.chatsdk.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.databinding.DataBindingUtil;

import org.ocpsoft.prettytime.PrettyTime;
import org.pmw.tinylog.Logger;

import java.util.Locale;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.databinding.ActivityChatWrapperBinding;
import co.chatsdk.ui.fragments.CKChatFragment;
import co.chatsdk.ui.fragments.ProfileFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CKChatWrapperActivity extends BaseActivity {

    ActivityChatWrapperBinding b;

    protected Thread thread;
    protected Bundle bundle;

    protected CKChatFragment fragment;

    @Override
    protected int getLayout() {
        return R.layout.activity_chat_wrapper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

        // Get the chat fragment
        fragment = (CKChatFragment) getSupportFragmentManager().findFragmentById(R.id.chatFragment);

        updateExtras(getIntent().getExtras());

        if(ChatSDK.config().theme != 0) {
            setTheme(ChatSDK.config().theme);
        }

        // Setting the default task description.
        setTaskDescription(getTaskDescriptionBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());

//        setContentView(getLayout());
//        ButterKnife.bind(this);


    }

    public void addListeners() {
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> b.chatActionBar.reload(thread)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .filter(networkEvent -> thread.containsUser(networkEvent.user))
                .subscribe(networkEvent -> {
                    reloadData();
                    b.chatActionBar.reload(thread);
                }));
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    String typingText = networkEvent.text;
                    if(typingText != null) {
                        typingText += getString(R.string.typing);
                    }
                    Logger.debug(typingText);
                    b.chatActionBar.setSubtitleText(thread, typingText);
                }));
    }

    public void initViews() {
        super.initViews();

        // Action bar
        b.chatActionBar.setOnClickListener(v -> openThreadDetailsActivity());

        setSupportActionBar(b.chatActionBar.getToolbar());
        b.chatActionBar.reload(thread);

    }

    /**
     * Open the thread details context, Admin user can change thread name an messageImageView there.
     */
    protected void openThreadDetailsActivity() {
        ChatSDK.ui().startThreadDetailsActivity(this, thread.getEntityID());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!updateThreadFromBundle(bundle)) {
            return;
        }

        if (thread.typeIs(ThreadType.Private1to1) && thread.otherUser() != null && ChatSDK.lastOnline() != null) {
            dm.add(ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((date, throwable) -> {
                        if (throwable == null && date != null) {
                            Locale current = getResources().getConfiguration().locale;
                            PrettyTime pt = new PrettyTime(current);
                            if (thread.otherUser().getIsOnline()) {
                                b.chatActionBar.setSubtitleText(thread, getString(R.string.online));
                            } else {
                                b.chatActionBar.setSubtitleText(thread, String.format(getString(R.string.last_seen__), pt.format(date)));
                            }
                        }
                    }));
        } else {
            b.chatActionBar.setSubtitleText(thread, null);
        }

        // Show a local notification if the text is from a different thread
        ChatSDK.ui().setLocalNotificationHandler(thread -> !thread.getEntityID().equals(this.thread.getEntityID()));

    }

    protected boolean updateThreadFromBundle(Bundle bundle) {

        if (bundle != null && (bundle.containsKey(Keys.IntentKeyThreadEntityID))) {
            this.bundle = bundle;
        }
        else {
            if (getIntent() == null || getIntent().getExtras() == null) {
                finish();
                return false;
            }
            this.bundle = getIntent().getExtras();
        }

        if (this.bundle.containsKey(Keys.IntentKeyThreadEntityID)) {
            String threadEntityID = this.bundle.getString(Keys.IntentKeyThreadEntityID);
            if(threadEntityID != null) {
                thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            }
        }

        if (thread == null) {
            finish();
            return false;
        }

        if (fragment != null) {
            fragment.setThread(thread);
        } else {
            finish();
        }

        return true;
    }

    protected void reloadData() {
        fragment.reloadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!updateThreadFromBundle(intent.getExtras()))
            return;

        fragment.clearData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        if (fragment.getMessagesListAdapter() != null && !fragment.getMessagesListAdapter().getSelectedMessages().isEmpty()) {
            b.chatActionBar.hideText();
        } else {
            b.chatActionBar.showText();
        }
        return result;
    }

    /**
     * Show the option popup when the menu key is pressed.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                fragment.showOptions();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
