package co.chatsdk.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
    protected void doneButtonPressed(List<User> users) {
        if (adapter.getSelectedCount() == 0) {
            showToast(getString(R.string.select_at_least_one_user));
            return;
        }

        showProgressDialog( getString(R.string.adding_users));

        dm.add(ChatSDK.thread().addUsersToThread(thread, users)
                .observeOn(AndroidSchedulers.mainThread())
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
