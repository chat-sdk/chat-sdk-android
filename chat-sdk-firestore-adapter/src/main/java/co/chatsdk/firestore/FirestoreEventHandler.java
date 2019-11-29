package co.chatsdk.firestore;

import org.joda.time.DateTime;

import java.util.Date;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.firebase.FirebaseEventHandler;
import io.reactivex.disposables.Disposable;
import sdk.chat.micro.MicroChatSDK;
import co.chatsdk.core.dao.Thread;
import sdk.chat.micro.message.TextMessage;

public class FirestoreEventHandler extends FirebaseEventHandler {

    public FirestoreEventHandler () {
        Disposable d = MicroChatSDK.shared().messageStream.subscribe(message -> {

            // Get the user
            Disposable d1 = UserHelper.fetchUser(message.fromId).subscribe(user -> {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(message.fromId);
                if (thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(message.fromId);
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    thread.setCreatorEntityId(message.fromId);

                    // Add the sender
                    thread.addUsers(user, ChatSDK.currentUser());
                }
//                if (!thread.containsMessageWithID(message.id)) {
                    Message chatSDKMessage = ChatSDK.db().createEntity(Message.class);
                    chatSDKMessage.setSender(user);
                    chatSDKMessage.setMessageStatus(MessageSendStatus.Delivered);
                    chatSDKMessage.setDate(new DateTime(message.date));
                    chatSDKMessage.setEntityID(message.id);
                    chatSDKMessage.setType(MessageType.Text);

                    // Make this more robust
                    chatSDKMessage.setText((String)message.getBody().get(TextMessage.TextKey));

                    thread.addMessage(chatSDKMessage);

                    eventSource.onNext(NetworkEvent.messageAdded(thread, chatSDKMessage));
//                }

            });
        });
    }

    protected void threadsOn (User user) {
    }
    protected void publicThreadsOn (User user) {
    }

}
