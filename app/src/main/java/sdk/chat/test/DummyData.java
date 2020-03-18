package sdk.chat.test;

import android.os.AsyncTask;

import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.Executor;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import io.reactivex.Completable;

public class DummyData {

    int threadCount = 1;
    int messageCount = 100;

    public DummyData() {

    }

    public DummyData(int messageCount) {
        this.messageCount = messageCount;
    }

    public DummyData(int threadCount, int messageCount) {
        this.threadCount = threadCount;
        this.messageCount = messageCount;

        ChatSDK.hook().addHook(Hook.sync(data -> {
            AsyncTask.execute(() -> {
                for (int i = 0; i < threadCount; i++) {
                    createThread();
                    Logger.trace("Thread " + i);
                }
            });
        }), HookEvent.DidAuthenticate);

    }

    public void create() {
        for (int i = 0; i < threadCount; i++) {
            createThread();
            Logger.debug("Thread " + i);
        }
    }

    public void createThread() {
        final Thread thread = ChatSDK.db().createEntity(Thread.class);

        User currentUser = ChatSDK.currentUser();
        thread.setCreator(currentUser);
        thread.setType(ThreadType.PrivateGroup);
        thread.setName("Test " + new Date().toString(), false);
        thread.setEntityID(UUID.randomUUID().toString());
        thread.update();

        thread.addUsers(currentUser);
        for (User user: ChatSDK.contact().contacts()) {
            thread.addUser(user);
        }

        // Add the messages
        for (int i = 0; i < messageCount; i++) {
            addMessage(thread, String.valueOf(i));
        }

        ChatSDK.events().source().onNext(NetworkEvent.threadAdded(thread));
    }

    public void addMessage(Thread thread, String text) {

        Message message = ChatSDK.db().createEntity(Message.class);

        ArrayList<User> contacts = new ArrayList<>(ChatSDK.contact().contacts());
        contacts.add(ChatSDK.currentUser());

        User sender = contacts.get(new Random().nextInt(contacts.size()));

        message.setSender(sender);

        message.setEntityID(UUID.randomUUID().toString());
        message.setMessageStatus(MessageSendStatus.Created, false);
        message.setDate(new DateTime());
        message.setType(MessageType.Text);

        message.setText(text + " - " + message.getDate().toString());

        for (User user: thread.getUsers()) {
            if (user.isMe() && sender.isMe()) {
                message.setUserReadStatus(user, ReadStatus.read(), new DateTime());
            } else {
                message.setUserReadStatus(user, ReadStatus.none(), new DateTime());
            }
        }

        thread.addMessage(message, false);

        message.update();
    }



}
