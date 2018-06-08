package co.chatsdk.android.app;

import android.view.Menu;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.ui.chat.ChatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CustomChatActivity extends ChatActivity {

    @Override
    protected void initViews() {
        super.initViews();
        if (messageListAdapter == null || !messageListAdapter.getClass().equals(CustomMessagesListAdapter.class)) {
            messageListAdapter = new CustomMessagesListAdapter(CustomChatActivity.this);
        }
        recyclerView.setAdapter(messageListAdapter);
    }

    @Override
    protected void onResume() {
        addUserToChatOnEnter = false;
        super.onResume();

        User currentUser = NM.currentUser();
        if (thread != null && thread.typeIs(ThreadType.Public)) {
            if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(currentUser.getEntityID())) {
                NM.thread().addUsersToThread(thread, currentUser)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CrashReportingCompletableObserver(disposableList));
            }
        }
    }

    @Override
    protected void onStop() {
        removeUserFromChatOnExit = false;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (StringUtils.isNotBlank(thread.getCreatorEntityId()) && thread.getCreatorEntityId().equals(NM.currentUser().getEntityID())) {
            return super.onCreateOptionsMenu(menu);
        } else {
            inflateMenuItems = false;
            return super.onCreateOptionsMenu(menu);
        }
    }

}
