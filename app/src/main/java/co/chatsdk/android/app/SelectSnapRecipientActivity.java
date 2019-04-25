package co.chatsdk.android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.ui.contacts.SelectContactActivity;
import co.chatsdk.ui.login.SplashScreenActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class SelectSnapRecipientActivity extends SelectContactActivity {

    private String theImagePath;
    protected Class snapChatActivity = co.chatsdk.android.app.SnapChatActivity.class;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        theImagePath = (String) i.getSerializableExtra("theImagePath");
    }

    @Override
    protected Single<Thread> createAndOpenThread (String name, List<User> users) {
        return ChatSDK.thread().createThread(name, users, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(thread -> {
                    ChatSDK.imageMessage().sendMessageWithImage(theImagePath, thread).subscribe(new Consumer<MessageSendProgress>() {
                        @Override
                        public void accept(MessageSendProgress messageSendProgress) throws Exception {
                            if(messageSendProgress.message.getMessageStatus() == MessageSendStatus.Sending) {
                                messageSendProgress.message.setValueForKey(20, "message-lifetime");
                            }
                        }
                    });
                    if (thread != null) {
                        this.startChatActivityForID(getApplicationContext(), thread.getEntityID());
                    }
                }).doOnError(throwable -> {
                    ToastHelper.show(getApplicationContext(), co.chatsdk.ui.R.string.create_snap_thread_with_users_fail_toast);
                });
    }

    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getSnapChatActivity());
        intent.putExtra(Keys.THREAD_ENTITY_ID, threadEntityID);
        startActivity(intent);
    }
//This step is a little unnecessary, but it mirrors the rest of the framework so when it comes time
// to move it to the framework it can be easily done.
    public Class getSnapChatActivity () {
        return snapChatActivity;
    }

}
