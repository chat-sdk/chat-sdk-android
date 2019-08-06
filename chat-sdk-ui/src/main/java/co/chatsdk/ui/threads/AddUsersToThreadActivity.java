package co.chatsdk.ui.threads;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.SelectContactActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AddUsersToThreadActivity extends SelectContactActivity {

    protected String threadEntityID = "";
    protected Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle(R.string.add_user_to_chat);
    }

    @Override
    protected void getDataFromBundle(Bundle bundle) {
        super.getDataFromBundle(bundle);
        threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID, threadEntityID);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.IntentKeyThreadEntityID, threadEntityID);
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    @Override
    protected void loadData () {
        final List<User> list = ChatSDK.contact().contacts();

        // Removing the users that is already inside the thread.
        if (threadEntityID != null && !threadEntityID.isEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
            List<User> threadUser = thread.getUsers();
            list.removeAll(threadUser);
        }

        adapter.setUsers(new ArrayList<>(list), true);
    }

    @Override
    protected void doneButtonPressed(List<User> users) {
        if (adapter.getSelectedCount() == 0) {
            showToast(getString(R.string.pick_friends_activity_no_users_selected_toast));
            return;
        }

        showProgressDialog( getString(R.string.pick_friends_activity_prog_dialog_add_to_convo_message));

        disposableList.add(ChatSDK.thread().addUsersToThread(thread, users)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    dismissProgressDialog();
                    finish();
                })
                .subscribe(() -> {
                    setResult(Activity.RESULT_OK);
                    if (animateExit) {
                        overridePendingTransition(R.anim.dummy, R.anim.slide_top_bottom_out);
                    }
                }, throwable -> {
                    ChatSDK.logError(throwable);
                    setResult(Activity.RESULT_CANCELED);
                }));
    }

}
