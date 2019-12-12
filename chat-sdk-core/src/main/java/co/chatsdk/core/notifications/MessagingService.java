package co.chatsdk.core.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.RemoteInput;

import co.chatsdk.core.dao.Thread;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;

public class MessagingService extends IntentService {

    public static String EXTRA_CONVERSATION_ENTITY_ID_KEY = "conversation_id";
    public static String REMOTE_INPUT_RESULT_KEY = "reply_input";
    public static String MESSAGING_SERVICE_NAME = "sdk.chat.MessagingService";

    public MessagingService () {
        this(MESSAGING_SERVICE_NAME);
    }

    public MessagingService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String threadEntityID = intent.getStringExtra(EXTRA_CONVERSATION_ENTITY_ID_KEY);

        final Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);

        String action = intent.getAction();

        if (action != null && action.equals(ActionKeys.REPLY)) {

            Bundle results = RemoteInput.getResultsFromIntent(intent);
            String message = results.getString(REMOTE_INPUT_RESULT_KEY);

            ChatSDK.thread().sendMessageWithText(message, thread).subscribe(new CrashReportingCompletableObserver());

        }
        if (action != null && action.equals(ActionKeys.MARK_AS_READ)) {
            if (ChatSDK.readReceipts() != null) {
                ChatSDK.readReceipts().markRead(thread);
                thread.markRead();
            }
        }
    }
}
