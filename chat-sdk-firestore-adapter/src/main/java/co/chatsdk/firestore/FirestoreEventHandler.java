package co.chatsdk.firestore;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Observable;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.firebase.FirebaseEventHandler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.micro.MicroChatSDK;
import co.chatsdk.core.dao.Thread;
import sdk.chat.micro.chat.GroupChat;
import sdk.chat.micro.message.TextMessage;
import sdk.chat.micro.rx.DisposableList;

public class FirestoreEventHandler extends FirebaseEventHandler {

    protected DisposableList disposableList = new DisposableList();

    public FirestoreEventHandler () {

        disposableList.add(MicroChatSDK.shared().getGroupChatAddedStream().subscribe(new Consumer<GroupChat>() {
            @Override
            public void accept(GroupChat groupChat) throws Exception {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(groupChat.getId());
                if (thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(groupChat.getId());
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    // TODO:
                    // Continue here
//                    thread.setCreatorEntityId(groupChat.);

                    // Add the sender
//                    thread.addUsers(user, ChatSDK.currentUser());
                }

                // User roles

                groupChat.getMessageStream().subscribe(message -> {

                    Message chatSDKMessage = ChatSDK.db().createEntity(Message.class);
//                    chatSDKMessage.setSender(user);
                    chatSDKMessage.setMessageStatus(MessageSendStatus.Delivered);
                    chatSDKMessage.setDate(new DateTime(message.date));
                    chatSDKMessage.setEntityID(message.id);

                    HashMap<String, Object> body = message.getBody();

                    Object metaObject = body.get(Keys.Meta);
                    if (metaObject instanceof HashMap) {
                        HashMap<String, Object> meta = new HashMap<>((HashMap) metaObject);
                        chatSDKMessage.setMetaValues(meta);
                    }

                    Object typeObject = body.get(Keys.Type);
                    if (typeObject instanceof Long) {
                        Integer type = ((Long) typeObject).intValue();
                        chatSDKMessage.setType(type);
                    }
                    if (typeObject instanceof Integer) {
                        Integer type = (Integer) typeObject;
                        chatSDKMessage.setType(type);
                    }

                });

            }
        }));

        disposableList.add(MicroChatSDK.shared().getMessageStream().subscribe(message -> {

            // Get the user
            disposableList.add(UserHelper.fetchUser(message.from).subscribe(user -> {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(message.from);
                if (thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(message.from);
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    thread.setCreatorEntityId(message.from);

                    // Add the sender
                    thread.addUsers(user, ChatSDK.currentUser());
                }

                //                if (!thread.containsMessageWithID(text.id)) {
                    Message chatSDKMessage = ChatSDK.db().createEntity(Message.class);
                    chatSDKMessage.setSender(user);
                    chatSDKMessage.setMessageStatus(MessageSendStatus.Delivered);
                    chatSDKMessage.setDate(new DateTime(message.date));
                    chatSDKMessage.setEntityID(message.id);

                HashMap<String, Object> body = message.getBody();

                Object metaObject = body.get(Keys.Meta);
                if (metaObject instanceof HashMap) {
                    HashMap<String, Object> meta = new HashMap<>((HashMap) metaObject);
                    chatSDKMessage.setMetaValues(meta);
                }

                Object typeObject = body.get(Keys.Type);
                if (typeObject instanceof Long) {
                    Integer type = ((Long) typeObject).intValue();
                    chatSDKMessage.setType(type);
                }
                if (typeObject instanceof Integer) {
                    Integer type = (Integer) typeObject;
                    chatSDKMessage.setType(type);
                }

                    // Make this more robust
//                    chatSDKMessage.setText((String)text.getBody().get(TextMessage.TextKey));

                    thread.addMessage(chatSDKMessage);

                    eventSource.onNext(NetworkEvent.messageAdded(thread, chatSDKMessage));
//                }

            }));
        }));
    }

    protected void threadsOn (User user) {
    }
    protected void publicThreadsOn (User user) {
    }

}
