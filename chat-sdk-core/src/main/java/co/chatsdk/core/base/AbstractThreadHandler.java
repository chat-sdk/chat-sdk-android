package co.chatsdk.core.base;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.sorter.ThreadsSorter;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    public Single<List<Message>> loadMoreMessagesForThread(final Message fromMessage, final Thread thread) {
        return Single.create((SingleOnSubscribe<List<Message>>) e -> {

            //
            Date messageDate = fromMessage != null ? fromMessage.getDate().toDate() : null;

            // First try to load the messages from the database
            List<Message> list = ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), ChatSDK.config().messagesToLoadPerBatch + 1, messageDate);
            e.onSuccess(list);
        }).subscribeOn(Schedulers.single());
    }

    /**
     * Preparing a text message,
     * This is only the build part of the send from here the message will passed to "sendMessage" Method.
     * From there the message will be uploaded to the server if the upload fails the message will be deleted from the local db.
     * If the upload is successful we will update the message entity so the entityId given from the server will be saved.
     * The message will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the message is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    public Observable<MessageSendProgress> sendMessageWithText(final String text, final Thread thread) {
        return Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {

            final Message message = newMessage(MessageType.Text, thread);
            message.setText(text);

            e.onNext(new MessageSendProgress(message));

            ObservableConnector<MessageSendProgress> connector = new ObservableConnector<>();
            connector.connect(implSendMessage(message), e);

        }).subscribeOn(Schedulers.single());

    }

    public static Message newMessage (int type, Thread thread) {
        Message message = ChatSDK.db().createEntity(Message.class);
        message.setSender(ChatSDK.currentUser());
        message.setMessageStatus(MessageSendStatus.Sending);
        message.setDate(new DateTime(System.currentTimeMillis()));
        message.setEntityID(UUID.randomUUID().toString());
        message.setType(type);
        thread.addMessage(message);
        return message;
    }

    public static Message newMessage (MessageType type, Thread thread) {
        return newMessage(type.ordinal(), thread);
    }

    /**
    /* Convenience method to save the message to the database then pass it to the token network adapter
     * send method so it can be sent via the network
     */
    public Observable<MessageSendProgress> implSendMessage(final Message message) {
        return Observable.create((ObservableOnSubscribe<MessageSendProgress>) e -> {
            message.update();
            message.getThread().update();

            if (ChatSDK.encryption() != null) {
                ChatSDK.encryption().encrypt(message);
            }

            e.onNext(new MessageSendProgress(message));
            e.onComplete();
        }).concatWith(sendMessage(message))
                .subscribeOn(Schedulers.single()).doOnComplete(() -> {
                    message.setMessageStatus(MessageSendStatus.Sent);
                    message.update();
                }).doOnError(throwable -> {
                    message.setMessageStatus(MessageSendStatus.Failed);
                    message.update();
                });
    }

    public int getUnreadMessagesAmount(boolean onePerThread){
        List<Thread> threads = getThreads(ThreadType.Private, false);

        int count = 0;
        for (Thread t : threads) {
            if (onePerThread) {
                if(!t.isLastMessageWasRead()) {
                    count++;
                }
            }
            else {
                count += t.getUnreadMessagesCount();
            }
        }
        return count;
    }

    public Single<Thread> createThread(String name, User... users) {
        return createThread(name, Arrays.asList(users));
    }

    public Single<Thread> createThread(List<User> users) {
        return createThread(null, users);
    }

    public Completable addUsersToThread(Thread thread, User... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    public Completable removeUsersFromThread(Thread thread, User... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    public List<Thread> getThreads(int type) {
        return getThreads(type, false);
    }

    public List<Thread> getThreads(int type, boolean allowDeleted) {
        return getThreads(type, allowDeleted, ChatSDK.config().showEmptyChats);
    }

    public List<Thread> getThreads(int type, boolean allowDeleted, boolean showEmpty){

        if(ThreadType.isPublic(type)) {
            return ChatSDK.db().fetchThreadsWithType(ThreadType.PublicGroup);
        }

        // We may access this method post authentication
        if(ChatSDK.currentUser() == null) {
            return new ArrayList<>();
        }

        List<Thread> threads = ChatSDK.db().fetchThreadsForUserWithID(ChatSDK.currentUser().getId());

        List<Thread> filteredThreads = new ArrayList<>();
        for(Thread thread : threads) {
            if(thread.typeIs(type) && (!thread.getDeleted() || allowDeleted)) {
                if (showEmpty || thread.getMessages().size() > 0) {
                    filteredThreads.add(thread);
                }
            }
        }

        // Sort the threads list before returning
        Collections.sort(filteredThreads, new ThreadsSorter());

        return filteredThreads;
    }

    public void sendLocalSystemMessage(String text, Thread thread) {

    }

    public void sendLocalSystemMessage(String text, CoreHandler.bSystemMessageType type, Thread thread) {

    }


}
