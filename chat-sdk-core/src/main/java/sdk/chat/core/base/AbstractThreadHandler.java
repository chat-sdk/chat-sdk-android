package sdk.chat.core.base;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.R;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.handlers.ThreadHandler;
import sdk.chat.core.interfaces.SystemMessageType;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    @Override
    public Single<List<Message>> loadMoreMessagesBefore(final Thread thread, @Nullable final Date before) {
        return loadMoreMessagesBefore(thread, before, true);
    }

    @Override
    public Single<List<Message>> loadMoreMessagesBefore(final Thread thread, @Nullable Date before, boolean loadFromServer) {
        return Single.defer(() -> {
            return Single.just(ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), null, before, ChatSDK.config().messagesToLoadPerBatch + 1));
        }).subscribeOn(RX.db());
    }

    @Override
    public Single<List<Message>> loadMoreMessagesAfter(Thread thread, @Nullable Date after, boolean loadFromServer) {
        return Single.defer(() -> {
            return Single.just(ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), after, null, 0));
        }).subscribeOn(RX.db());
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

    public Single<Thread> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL) {
        return createThread(name, theUsers, type, entityID, imageURL, null);
    }

    public Single<Thread> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL, Map<String, Object> meta) {
        return createThread(name, theUsers, type, entityID, imageURL, meta);
    }

    public Single<Thread> create1to1Thread(User otherUser, @Nullable Map<String, Object> meta) {
        return createThread(null, Collections.singletonList(otherUser), ThreadType.Private1to1, null, null, meta);
    }

    public Single<Thread> createPrivateGroupThread(@Nullable String name, List<User> users, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta) {
        return createThread(name, users, ThreadType.PrivateGroup, entityID, imageURL, meta);
    }

    public Message newMessage(int type, Thread thread) {
        Message message = ChatSDK.db().createEntity(Message.class);
        message.setSender(ChatSDK.currentUser());
        message.setDate(new Date());
        message.setEntityID(UUID.randomUUID().toString());
        message.setType(type);
        message.setMessageStatus(MessageSendStatus.None, false);

        if (!thread.typeIs(ThreadType.Public)) {
            for (User user: thread.getUsers()) {
                if (user.isMe()) {
                    message.setUserReadStatus(user, ReadStatus.read(), new Date(), false);
                } else {
                    if (ChatSDK.thread().hasVoice(thread, user)) {
                        message.setUserReadStatus(user, ReadStatus.none(), new Date(), false);
                    }
                }
            }
        }

        thread.addMessage(message, false);

        return message;
    }

    public Message newMessage(MessageType type, Thread thread) {
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
            return new MessageSendRig(newMessage, thread).run();
        }).subscribeOn(RX.db());
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
    public Single<Integer> getUnreadMessagesAmount(boolean onePerThread){
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
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
            emitter.onSuccess(count);
        }).subscribeOn(RX.db());
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
    public boolean canAddUsersToThread(Thread thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
    }

    @Override
    public Completable addUsersToThread(Thread thread, User... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    public Completable addUsersToThread(final Thread thread, final List<User> users) {
        return addUsersToThread(thread, users);
    }

    @Override
    public Completable removeUsersFromThread(Thread thread, User... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    @Override
    public boolean canRemoveUsersFromThread(Thread thread, List<User> users) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
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
    public Completable deleteThread(Thread thread) {
        return Completable.create(emitter -> {
            for (Message m: thread.getMessages()) {
                thread.removeMessage(m);
                m.delete();
            }
            thread.setLoadMessagesFrom(new Date());
            thread.setDeleted(true);
            emitter.onComplete();

//            return thread.deleteThread().doOnComplete(() -> {
//                eventSource.onNext(NetworkEvent.threadRemoved(thread.getModel()));
//            });

        }).subscribeOn(RX.db());
    }

    @Override
    public Completable deleteMessages(Message... messages) {
        return deleteMessages(Arrays.asList(messages));
    }

    @Override
    public boolean canDeleteMessage(Message message) {
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
    public Completable replyToMessage(Thread thread, Message message, String reply) {
        return Completable.defer(() -> {
            Message newMessage = newMessage(MessageType.Text, thread);
            // If this is already a reply, then don't copy the meta data
            if (message.isReply()) {
                newMessage.setText(message.getReply());
            } else {
                newMessage.setMetaValues(message.getMetaValuesAsMap());
                newMessage.setValueForKey(message.getType(), Keys.Type);
                newMessage.setValueForKey(message.getEntityID(), Keys.Id);
            }
            newMessage.setValueForKey(reply, Keys.Reply);
            return new MessageSendRig(newMessage, thread).run();

        }).subscribeOn(RX.db());
    }

    // Moderation

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
        return Completable.error(ChatSDK.getException(R.string.feature_not_supported));
    }

    @Override
    public List<String> availableRoles(Thread thread, User user) {
        return new ArrayList<>();
    }

    @Override
    public Completable grantVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public Completable revokeVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public boolean hasVoice(Thread thread, User user) {
        return true;
    }

    @Override
    public boolean isBanned(Thread thread, User user) {
        return false;
    }

    @Override
    public boolean canChangeVoice(Thread thread, User user) {
        return false;
    }

    @Override
    public Completable grantModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public Completable revokeModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public boolean canChangeModerator(Thread thread, User user) {
        return false;
    }

    @Override
    public boolean isModerator(Thread thread, User user) {
        return false;
    }

    public String localizeRole(String role) {
        return localizeRoles(Collections.singletonList(role)).get(0);
    }

    @Override
    public List<String> localizeRoles(String... roles) {
        return localizeRoles(Arrays.asList(roles));
    }

    @Override
    public List<String> localizeRoles(List<String> roles) {
        return roles;
    }

    public boolean canLeaveThread(Thread thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.containsUser(ChatSDK.currentUser());
    }

    @Override
    public boolean canEditThreadDetails(Thread thread) {
        return thread.getCreator().isMe() && !thread.typeIs(ThreadType.Private1to1);
    }

    @Override
    public Completable pushThreadMeta(Thread thread) {
        return Completable.complete();
    }

}
