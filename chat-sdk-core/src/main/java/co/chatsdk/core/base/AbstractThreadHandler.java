package co.chatsdk.core.base;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import co.chatsdk.core.R;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.sorter.ThreadsSorter;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.CoreHandler;
import co.chatsdk.core.handlers.ThreadHandler;
import co.chatsdk.core.interfaces.SystemMessageType;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.types.ReadStatus;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    @Override
    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread) {
        return loadMoreMessagesForThread(fromDate, thread, true);
    }

    @Override
    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread, boolean loadFromServer) {
        return Single.just(ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), ChatSDK.config().messagesToLoadPerBatch + 1, fromDate)).subscribeOn(Schedulers.io());
    }

    /**
     * Preparing a text text,
     * This is only the build part of the send from here the text will passed to "sendMessage" Method.
     * From there the text will be uploaded to the server if the upload fails the text will be deleted from the local db.
     * If the upload is successful we will update the text entity so the entityId given from the server will be saved.
     * The text will be received before sending in the onMainFinished Callback with a Status that its in the sending process.
     * When the text is fully sent the status will be changed and the onItem callback will be invoked.
     * When done or when an error occurred the calling method will be notified.
     */
    @Override
    public Completable sendMessageWithText(final String text, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Text), thread, message -> message.setText(text)).run();
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users) {
        return createThread(name, users, -1);
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users, final int type) {
        return createThread(name, users, type, null);
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, int type, String entityID) {
        return createThread(name, users, type, entityID, null);
    }

    public static Message newMessage (int type, Thread thread) {
        Message message = ChatSDK.db().createEntity(Message.class);
        message.setSender(ChatSDK.currentUser());
        message.setDate(new DateTime(System.currentTimeMillis()));
        message.setEntityID(UUID.randomUUID().toString());
        message.setType(type);

        for (User user: thread.getUsers()) {
            if (user.isMe()) {
                message.setUserReadStatus(user, ReadStatus.read(), new DateTime());
            } else {
                message.setUserReadStatus(user, ReadStatus.none(), new DateTime());
            }
        }

        thread.addMessage(message);

        message.setMessageStatus(MessageSendStatus.Created);

        return message;
    }

    public static Message newMessage (MessageType type, Thread thread) {
        return newMessage(type.ordinal(), thread);
    }

    /**
    /* Convenience method to save the text to the database then pass it to the token network adapter
     * send method so it can be sent via the network
     */
    @Override
    public Completable forwardMessage(Thread thread, Message message) {
        return Completable.defer(() -> {
            Message newMessage = newMessage(message.getType(), thread);
            newMessage.setMetaValues(message.getMetaValuesAsMap());
            newMessage.setMessageStatus(MessageSendStatus.WillSend);
            return sendMessage(newMessage);
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable forwardMessages(Thread thread, Message... messages) {
        return forwardMessages(thread, Arrays.asList(messages));
    }

    @Override
    public Completable forwardMessages(Thread thread, List<Message> messages) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (Message message: messages) {
            completables.add(forwardMessage(thread, message));
        }
        return Completable.concat(completables);
    }

    @Override
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

    @Override
    public Single<Thread> createThread(String name, User... users) {
        return createThread(name, Arrays.asList(users));
    }

    @Override
    public Single<Thread> createThread(List<User> users) {
        return createThread(null, users);
    }

    @Override
    public Completable addUsersToThread(Thread thread, User... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    @Override
    public Completable removeUsersFromThread(Thread thread, User... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    @Override
    public List<Thread> getThreads(int type) {
        return getThreads(type, false);
    }

    @Override
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
            threads = ChatSDK.db().fetchThreadsForCurrentUser();
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
//        Collections.sort(filteredThreads, new ThreadsSorter());

        return filteredThreads;
    }

    @Override
    public void sendLocalSystemMessage(String text, Thread thread) {
        sendLocalSystemMessage(text, SystemMessageType.standard, thread);
    }

    @Override
    public void sendLocalSystemMessage(String text, SystemMessageType type, Thread thread) {
        new MessageSendRig(new MessageType(MessageType.System), thread, message -> {
            message.setText(text);
            message.setValueForKey(type, Keys.Type);
        }).localOnly().run().subscribe(ChatSDK.events());
    }

    @Override
    public Completable mute(Thread thread) {
        return Completable.complete();
    }

    @Override
    public Completable unmute(Thread thread) {
        return Completable.complete();
    }

    @Override
    public boolean rolesEnabled(Thread thread) {
        return false;
    }

    @Override
    public boolean canChangeRole(Thread thread, User user) {
        return false;
    }

    @Override
    public String roleForUser(Thread thread, User user) {
        return null;
    }

    @Override
    public Completable setRole(String role, Thread thread, User user) {
        return Completable.error(new Throwable(ChatSDK.shared().getString(R.string.feature_not_supported)));
    }

    @Override
    public List<String> availableRoles(Thread thread, User user) {
        return new ArrayList<>();
    }

    @Override
    public Completable deleteThread(Thread thread) {
        return Completable.create(emitter -> {
            for (Message m: thread.getMessages()) {
                thread.removeMessage(m);
                m.delete();
            }
            thread.setDeleted(true);

            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteMessages(Message... messages) {
        return deleteMessages(Arrays.asList(messages));
    }

    @Override
    public boolean deleteMessageEnabled(Message message) {
        return message.getSender().isMe();
    }

    @Override
    public Completable deleteMessages(List<Message> messages) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (Message message: messages) {
            completables.add(deleteMessage(message));
        }
        return Completable.merge(completables);
    }

    @Override
    public boolean addUsersEnabled(Thread thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
    }

    @Override
    public boolean removeUsersEnabled(Thread thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
    }

    @Override
    public Completable replyToMessage(Thread thread, Message message, String reply) {
        return Completable.defer(() -> {
            Message newMessage = newMessage(MessageType.Text, thread);
            newMessage.setMetaValues(message.getMetaValuesAsMap());
            newMessage.setValueForKey(reply, Keys.Reply);
            newMessage.setMessageStatus(MessageSendStatus.WillSend);
            return sendMessage(newMessage);
        }).subscribeOn(Schedulers.io());
    }

    // Moderation
    @Override
    public Completable grantVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable revokeVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean hasVoice(Thread thread, User user) {
        return true;
    }

    @Override
    public boolean canChangeVoice(Thread thread, User user) {
        return false;
    }

    @Override
    public Completable grantModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable revokeModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean canChangeModerator(Thread thread, User user) {
        return false;
    }

    @Override
    public boolean isModerator(Thread thread, User user) {
        return false;
    }

}
