package sdk.chat.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.guru.common.RX;

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
        setMultiSelectEnabled(true);
    }

    @Override
    protected void loadData () {
        final List<User> list = ChatSDK.contact().contacts();
//        List<User> threadUser = thread.getUsers();
//        list.removeAll(threadUser);
        adapter.setUsers(new ArrayList<>(list), true);
    }

    @Override
    protected void doneButtonPressed(List<UserListItem> users) {
        List<Completable> completables = new ArrayList<>();

        for (User user: User.convertIfPossible(users)) {
            completables.add(ChatSDK.thread().createThread(Arrays.asList(user))
                    .flatMapCompletable(thread -> ChatSDK.thread().forwardMessages(thread, messages)));
        }

        Completable.merge(completables).observeOn(RX.main()).doOnComplete(() -> {
            setResult(Activity.RESULT_OK);
            finish();
        }).doOnError(throwable -> {
            Intent intent = new Intent();
            intent.putExtra(Keys.IntentKeyErrorMessage, throwable.getLocalizedMessage());
            setResult(RESULT_ERROR, intent);
            finish();
        }).subscribe(this);
    }
}
