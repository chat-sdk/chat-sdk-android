package co.chatsdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.core.dao.Thread;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ForwardMessageActivity extends SelectContactActivity {

    public static int RESULT_ERROR = 100;

    protected Thread thread;
    protected ArrayList<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // Get the text
        String threadEntityID = getIntent().getStringExtra(Keys.IntentKeyThreadEntityID);
        if (threadEntityID != null && !threadEntityID.isEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        }
        if (thread == null) {
            finish();
        }

        List<String> messageEntityIDs = getIntent().getExtras().getStringArrayList(Keys.IntentKeyMessageEntityIDs);

        for (String messageEntityID: messageEntityIDs) {
            Message message = ChatSDK.db().fetchEntityWithEntityID(messageEntityID, Message.class);
            if (message != null) {
                messages.add(message);
            }
        }

        if (messages.isEmpty()) {
            finish();
        }

        super.onCreate(savedInstanceState);

        setActionBarTitle(R.string.forward_message);
        setMultiSelectEnabled(false);
    }

    @Override
    protected void loadData () {
        final List<User> list = ChatSDK.contact().contacts();
        List<User> threadUser = thread.getUsers();
        list.removeAll(threadUser);
        adapter.setUsers(new ArrayList<>(list), true);
    }

    @Override
    protected void doneButtonPressed(List<User> users) {
        dm.add(ChatSDK.thread().createThread(users).flatMapCompletable(thread -> ChatSDK.thread().forwardMessages(thread, messages))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    setResult(Activity.RESULT_OK);
                    finish();
                }, throwable -> {
                    Intent intent = new Intent();
                    intent.putExtra(Keys.IntentKeyErrorMessage, throwable.getLocalizedMessage());
                    setResult(RESULT_ERROR, intent);
                    finish();
                }));
    }
}
