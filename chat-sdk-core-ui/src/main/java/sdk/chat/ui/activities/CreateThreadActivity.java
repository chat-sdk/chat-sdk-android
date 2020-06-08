package sdk.chat.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.RX;

public class CreateThreadActivity extends SelectContactActivity {

    protected Thread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(R.string.new_chat);
        setMultiSelectEnabled(UIModule.config().groupsEnabled);
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    @Override
    protected void doneButtonPressed(List<UserListItem> users) {
        if (adapter.getSelectedCount() == 0) {
            showSnackbar(getString(R.string.select_at_least_one_user));
            return;
        }

        // If there are more than 2 users then show a dialog to enter the name
        if(users.size() > 1) {
            ArrayList<String> userEntityIDs = new ArrayList<>();
            for (UserListItem u : users) {
                userEntityIDs.add(u.getEntityID());
            }
//            finish();
            ChatSDK.ui().startEditThreadActivity(this, null, userEntityIDs);
        }
        else {
            createAndOpenThread("", users);
        }
    }

    protected void createAndOpenThread (String name, List<UserListItem> users) {
        dm.add(ChatSDK.thread()
                .createThread(name, User.convertIfPossible(users))
                .observeOn(RX.main())
                .subscribe(thread -> {
                    ChatSDK.ui().startChatActivityForID(this, thread.getEntityID());
                    finish();
                }, this));
    }



}
