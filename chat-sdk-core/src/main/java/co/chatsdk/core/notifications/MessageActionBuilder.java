package co.chatsdk.core.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MessageActionBuilder {

    public static int ReplyOffset = 100000000;
    public static int MarkAsReadOffset = 200000000;

    public Integer getReplyId(Long threadId) {
        return ((Long)(threadId + ReplyOffset)).intValue();
    }

    public Integer getMarkReadId(Long threadId) {
        return ((Long)(threadId + MarkAsReadOffset)).intValue();
    }

    public RemoteInput createReplyRemoteInput() {
        return new RemoteInput.Builder(MessagingService.REMOTE_INPUT_RESULT_KEY).build();
    }

    public Intent createReplyIntent (Context context, String threadEntityID) {

        Intent intent = new Intent(context, MessagingService.class);

        intent.setAction(ActionKeys.REPLY);

        intent.putExtra(MessagingService.EXTRA_CONVERSATION_ENTITY_ID_KEY, threadEntityID);

        return intent;
    }

    public Intent createMarkAsReadIntent(Context context, String threadEntityID) {

        Intent intent = new Intent(context, MessagingService.class);

        intent.setAction(ActionKeys.MARK_AS_READ);
        intent.putExtra(MessagingService.EXTRA_CONVERSATION_ENTITY_ID_KEY, threadEntityID);

        return intent;
    }

    public NotificationCompat.Action createReplyAction (Context context, String threadEntityID, int replyID) {
        Intent replyIntent = createReplyIntent(context, threadEntityID);
        PendingIntent replyPendingIntent = PendingIntent.getService(context, replyID, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .setShowsUserInterface(false)
                .addRemoteInput(createReplyRemoteInput())
                .build();

        return replyAction;
    }

    public NotificationCompat.Action createMarkAsReadAction (Context context, String threadEnityID, int markAsReadId) {
        Intent markAsReadIntent = createMarkAsReadIntent(context, threadEnityID);
        PendingIntent markAsReadPendingIntent = PendingIntent.getService(context, markAsReadId, markAsReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action markAsReadAction = new NotificationCompat.Action.Builder(0, "Mark as Read", markAsReadPendingIntent)
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                .setShowsUserInterface(false)
                .build();

        return markAsReadAction;
    }

    public NotificationCompat.MessagingStyle createMessagingStyle (Thread thread) {

        User user = ChatSDK.currentUser();

        Person person = new Person.Builder()
                .setName(user.getName())
                .setIcon(null)
                .setKey(user.getEntityID())
                .build();

        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle(person);

        style.setConversationTitle(thread.getDisplayName());
        style.setGroupConversation(thread.typeIs(ThreadType.Group));

        for (Message message : thread.getMessages()) {
            if (!message.readStatusForUser(ChatSDK.currentUser()).is(ReadStatus.read())) {
                Person sender = new Person.Builder()
                        .setName(message.getSender().getName())
                        .setIcon(null)
                        .setKey(message.getSender().getEntityID())
                        .build();

                style.addMessage(message.getText(), message.getDate().toDate().getTime(), sender);
            }
        }

        return style;
    }

    public void addActions(NotificationCompat.Builder builder, Context context, Thread thread) {

        NotificationCompat.Action replyAction = createReplyAction(context, thread.getEntityID(), getReplyId(thread.getId()));
        NotificationCompat.Action markAsReadAction = createMarkAsReadAction(context, thread.getEntityID(), getMarkReadId(thread.getId()));
        NotificationCompat.MessagingStyle style = createMessagingStyle(thread);

        builder.setStyle(style);
        builder.addAction(replyAction);
        builder.addAction(markAsReadAction);

    }

}
