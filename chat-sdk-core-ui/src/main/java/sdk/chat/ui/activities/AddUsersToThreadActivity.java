package sdk.chat.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.guru.common.RX;

public class AddUsersToThreadActivity extends SelectContactActivity {

    protected Thread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(R.string.add_user_to_chat);
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    @Override
    protected void loadData () {
        final List<User> list = ChatSDK.contact().contacts();

        Object threadEntityIDObject = extras.get(Keys.IntentKeyThreadEntityID);;
        if (threadEntityIDObject instanceof String) {
            String threadEntityID = (String) threadEntityIDObject;

            // Removing the users that is already inside the thread.
            if (!threadEntityID.isEmpty()) {
                thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
                List<User> threadUser = thread.getUsers();
                list.removeAll(threadUser);
            }
        }

        adapter.setUsers(new ArrayList<>(list), true);
    }

    @Override
    protected void doneButtonPressed(List<UserListItem> users) {
        if (adapter.getSelectedCount() == 0) {
            showToast(getString(R.string.select_at_least_one_user));
            return;
        }

        showProgressDialog( getString(R.string.adding_users));

        dm.add(ChatSDK.thread().addUsersToThread(thread, User.convertIfPossible(users))
                .observeOn(RX.main())
                .doFinally(() -> {
                    dismissProgressDialog();
                    finish();
                })
                .subscribe(() -> {
                    setResult(Activity.RESULT_OK);
                }, throwable -> {
                    showToast(throwable.getLocalizedMessage());
                    setResult(Activity.RESULT_CANCELED);
                }));
    }

}
