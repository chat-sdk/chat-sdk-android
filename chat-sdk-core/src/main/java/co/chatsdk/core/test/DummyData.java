package co.chatsdk.core.test;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import co.chatsdk.core.base.AbstractCoreHandler;
import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;

public class DummyData {
    
    public DummyData() {
        ChatSDK.hook().addHook(new Hook(data -> Completable.create(emitter -> createDummyMessages())), HookEvent.DidAuthenticate);
    }

    public void createDummyMessages() {

        final Thread thread = ChatSDK.db().createEntity(Thread.class);

        User currentUser = ChatSDK.currentUser();
        thread.setCreator(currentUser);
        thread.setCreatorEntityId(currentUser.getEntityID());
        thread.setType(ThreadType.PrivateGroup);
        thread.setName("Test " + new Date().toString());
        thread.setEntityID(UUID.randomUUID().toString());
        thread.update();

        thread.addUsers(currentUser);
        for (User user: ChatSDK.contact().contacts()) {
            thread.addUser(user);
        }

        // Add the messages
        for (int i = 0; i < 300; i++) {
            addMessage(thread);
        }

        ChatSDK.events().source().onNext(NetworkEvent.threadAdded(thread));

    }

    public void addMessage(Thread thread) {

        Message message = ChatSDK.db().createEntity(Message.class);

        ArrayList<User> contacts = new ArrayList<>(ChatSDK.contact().contacts());
        contacts.add(ChatSDK.currentUser());

        User sender = contacts.get(new Random().nextInt(contacts.size()));

        message.setSender(sender);

        message.setEntityID(UUID.randomUUID().toString());
        message.setMessageStatus(MessageSendStatus.Created);
        message.setDate(new DateTime());
        message.setType(MessageType.Text);

        message.setText(UUID.randomUUID().toString());

        for (User user: thread.getUsers()) {
            if (user.isMe() && sender.isMe()) {
                message.setUserReadStatus(user, ReadStatus.read(), new DateTime());
            } else {
                message.setUserReadStatus(user, ReadStatus.none(), new DateTime());
            }
        }

        thread.addMessage(message);

        message.update();
    }



}
