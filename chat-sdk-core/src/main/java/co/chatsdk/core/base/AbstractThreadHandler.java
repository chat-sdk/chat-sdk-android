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
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread) {
        return loadMoreMessagesForThread(fromDate, thread, true);
    }

    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread, boolean loadFromServer) {
        return Single.just(ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), ChatSDK.config().messagesToLoadPerBatch + 1, fromDate)).subscribeOn(Schedulers.single());
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
    public Completable sendMessageWithText(final String text, final Thread thread) {
        return new MessageSendRig (new MessageType(MessageType.Text), thread, message -> message.setText(text)).run();
    }

    public static Message newMessage (int type, Thread thread) {
        Message message = ChatSDK.db().createEntity(Message.class);
        message.setSender(ChatSDK.currentUser());
        message.setMessageStatus(MessageSendStatus.Created);
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
    public Completable forwardMessage(Message message, Thread thread) {
        return Single.just(newMessage(message.getType(), thread)).flatMapCompletable(newMessage -> {
            newMessage.setMetaValues(message.getMetaValuesAsMap());

            newMessage.setMessageStatus(MessageSendStatus.WillSend);
            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));
            newMessage.setMessageStatus(MessageSendStatus.Sending);
            ChatSDK.events().source().onNext(NetworkEvent.messageSendStatusChanged(new MessageSendProgress(message)));

            return sendMessage(newMessage);

        }).subscribeOn(Schedulers.single());
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

        // We may access this method post authentication
        if(ChatSDK.currentUser() == null) {
            return new ArrayList<>();
        }

        List<Thread> threads;

        if(ThreadType.isPublic(type)) {
            threads =  ChatSDK.db().fetchThreadsWithType(ThreadType.PublicGroup);
        } else {
            threads = ChatSDK.db().fetchThreadsForUserWithID(ChatSDK.currentUser().getId());
        }

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
