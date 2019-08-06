package co.chatsdk.ui.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class ForwardMessageActivity extends SelectContactActivity {

    public static int RESULT_ERROR = 100;

    protected Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get the message
        String messageEntityID = getIntent().getStringExtra(Keys.IntentKeyMessageEntityID);
        if (messageEntityID != null && !messageEntityID.isEmpty()) {
            message = ChatSDK.db().fetchEntityWithEntityID(messageEntityID, Message.class);
        }
        if (message == null) {
            finish();
        }

        super.onCreate(savedInstanceState);

        setActionBarTitle(R.string.forward_message);
        setMultiSelectEnabled(false);
    }

    @Override
    protected void loadData () {
        final List<User> list = ChatSDK.contact().contacts();
        List<User> threadUser = message.getThread().getUsers();
        list.removeAll(threadUser);
        adapter.setUsers(new ArrayList<>(list), true);
    }

    @Override
    protected void doneButtonPressed(List<User> users) {
        disposableList.add(ChatSDK.thread().createThread(users).flatMapCompletable(thread -> ChatSDK.thread().forwardMessage(message, thread))
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
