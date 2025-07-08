package sdk.chat.core.base;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.R;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
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
import sdk.chat.core.utils.GoogleUtils;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public abstract class AbstractThreadHandler implements ThreadHandler {

    @Override
    public Single<List<Message>> loadMoreMessagesBefore(final ThreadX thread, @Nullable final Date before) {
        return loadMoreMessagesBefore(thread, before, true);
    }

    @Override
    public Single<List<Message>> loadMoreMessagesBefore(final ThreadX thread, @Nullable Date before, boolean loadFromServer) {
        return Single.defer(() -> {
            return Single.just(ChatSDK.db().fetchMessagesForThreadWithID(thread.getId(), null, before, ChatSDK.config().messagesToLoadPerBatch + 1));
        }).subscribeOn(RX.db());
    }

    @Override
    public Single<List<Message>> loadMoreMessagesAfter(ThreadX thread, @Nullable Date after, boolean loadFromServer) {
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
    public Completable sendMessageWithText(final String text, final ThreadX thread) {
        return new MessageSendRig(new MessageType(MessageType.Text), thread, message -> message.setText(text)).run();
    }

//    @Override
//    public Completable sendSilentMessage(final Map<String, String> data, final Thread thread) {
//        return new MessageSendRig(new MessageType(MessageType.Silent), thread, message -> {
//            message.setMetaValues(data);
//        }).run().doFinally(() -> {
//            ChatSDK.db().dele
//        });
//    }

    @Override
    public Single<ThreadX> createThread(final String name, final List<User> users) {
        return createThread(name, users, ThreadType.None);
    }

    @Override
    public Single<ThreadX> createThread(final String name, final List<User> users, final int type) {
        return createThread(name, users, type, null);
    }

    @Override
    public Single<ThreadX> createThread(String name, List<User> users, int type, String entityID) {
        return createThread(name, users, type, entityID, null);
    }

    public Single<ThreadX> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL) {
        return createThread(name, theUsers, type, entityID, imageURL, null);
    }

    public abstract Single<ThreadX> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL, Map<String, Object> meta);

    public Single<ThreadX> create1to1Thread(User otherUser, @Nullable Map<String, Object> meta) {
        return createThread(null, Collections.singletonList(otherUser), ThreadType.Private1to1, null, null, meta);
    }

    public Single<ThreadX> createPrivateGroupThread(@Nullable String name, List<User> users, @Nullable String entityID, @Nullable String imageURL, @Nullable Map<String, Object> meta) {
        return createThread(name, users, ThreadType.PrivateGroup, entityID, imageURL, meta);
    }

    public Message newMessage(int type, ThreadX thread, boolean notify) {
        Message message = new Message();
        message.setSender(ChatSDK.currentUser());
        message.setDate(new Date());

        message.setEntityID(generateNewMessageID(thread));
        message.setType(type);
        message.setMessageStatus(MessageSendStatus.Initial, false);
        message.setIsRead(true);

        ChatSDK.db().insertOrReplaceEntity(message);

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

        // TODO: Message Flow
        thread.addMessage(message, notify, true, false);

        return message;
    }

    public Message newMessage(MessageType type, ThreadX thread, boolean notify) {
        return newMessage(type.ordinal(), thread, notify);
    }

    /**
    /* Convenience method to save the text to the database then pass it to the token network adapter
     * send method so it can be sent via the network
     */
    @Override
    public Completable forwardMessage(ThreadX thread, Message message) {
        return Completable.defer(() -> {
            Message newMessage = newMessage(message.getType(), thread, false);
            newMessage.setMetaValues(message.getMetaValuesAsMap());
            return new MessageSendRig(newMessage, thread).run();
        }).subscribeOn(RX.db());
    }

    @Override
    public Completable forwardMessages(ThreadX thread, Message... messages) {
        return forwardMessages(thread, Arrays.asList(messages));
    }

    @Override
    public Completable forwardMessages(ThreadX thread, List<Message> messages) {
        ArrayList<Completable> completables = new ArrayList<>();
        for (Message message: messages) {
            completables.add(forwardMessage(thread, message));
        }
        return Completable.concat(completables);
    }

    @Override
    public Single<Integer> getUnreadMessagesAmount(boolean onePerThread){
        return Single.create((SingleOnSubscribe<Integer>) emitter -> {
            List<ThreadX> threads = getThreads(ThreadType.Private, false);

            int count = 0;
            for (ThreadX t : threads) {
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
    public Single<ThreadX> createThread(String name, User... users) {
        return createThread(name, Arrays.asList(users));
    }

    @Override
    public Single<ThreadX> createThread(List<User> users) {
        return createThread(null, users);
    }

    @Override
    public boolean canAddUsersToThread(ThreadX thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
    }

    @Override
    public Completable addUsersToThread(ThreadX thread, User... users) {
        return addUsersToThread(thread, Arrays.asList(users));
    }

    public Completable addUsersToThread(final ThreadX thread, final List<User> users) {
        return addUsersToThread(thread, users);
    }

    @Override
    public Completable removeUsersFromThread(ThreadX thread, User... users) {
        return removeUsersFromThread(thread, Arrays.asList(users));
    }

    @Override
    public boolean canRemoveUserFromThread(ThreadX thread, User user) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.getCreator() != null && thread.getCreator().isMe();
    }

    @Override
    public boolean canRemoveUsersFromThread(ThreadX thread, List<User> users) {
        for (User user: users) {
            if (!canRemoveUserFromThread(thread, user)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public List<ThreadX> getThreads(int type) {
        return getThreads(type, false);
    }

    @Override
    public List<ThreadX> getThreads(int type, boolean allowDeleted) {
        return getThreads(type, allowDeleted, ChatSDK.config().showEmptyChats);
    }

    public List<ThreadX> getThreads(int type, boolean allowDeleted, boolean showEmpty){

        // We may access this method post authentication
        if(ChatSDK.currentUser() == null) {
            return new ArrayList<>();
        }

        List<ThreadX> threads;

        if(ThreadType.isPublic(type)) {
            threads =  ChatSDK.db().fetchThreadsWithType(ThreadType.PublicGroup);
        } else {
            threads = ChatSDK.db().fetchThreadsForCurrentUser();
        }

        List<ThreadX> filteredThreads = new ArrayList<>();
        for(ThreadX thread : threads) {
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
    public void sendLocalSystemMessage(String text, ThreadX thread) {
        sendLocalSystemMessage(text, SystemMessageType.standard, thread);
    }

    @Override
    public void sendLocalSystemMessage(String text, SystemMessageType type, ThreadX thread) {
        new MessageSendRig(new MessageType(MessageType.System), thread, message -> {
            message.setText(text);
            message.setValueForKey(type, Keys.Type);
        }).localOnly().run().subscribe(ChatSDK.events());
    }

    @Override
    public Completable deleteThread(ThreadX thread) {
        return Completable.create(emitter -> {
            thread.removeMessagesAndMarkDeleted();
//            for (Message m: thread.getMessages()) {
//                thread.removeMessage(m);
//                m.cascadeDelete();
//            }
//            thread.setLoadMessagesFrom(new Date());
//            thread.setDeleted(true);
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
    public Completable replyToMessage(ThreadX thread, Message message, String reply) {
        return Completable.defer(() -> {

//            return new MessageSendRig(new MessageType(MessageType.Text), thread, m -> {
//
//            }).run();

            Message newMessage = newMessage(MessageType.Text, thread, false);
            // If this is already a reply, then don't copy the meta data
            if (message.isReply()) {
                newMessage.setText(message.getReply());
            } else {
                newMessage.setMetaValues(message.getMetaValuesAsMap());
                newMessage.setValueForKey(message.getType(), Keys.Type);
//                newMessage.setType(message.getType());
                newMessage.setValueForKey(message.getEntityID(), Keys.Id);
                newMessage.setValueForKey(message.getSender().getEntityID(), Keys.From);
            }
            newMessage.setValueForKey(reply, Keys.Reply);

            if (message.typeIs(MessageType.Location)) {

                double longitude = message.doubleForKey(Keys.MessageLongitude);
                double latitude = message.doubleForKey(Keys.MessageLatitude);
                int size = ChatSDK.config().replyThumbnailSize;

                newMessage.setValueForKey(GoogleUtils.getMapImageURL(latitude, longitude, size, size), Keys.ImageUrl);
            }

            ChatSDK.db().update(newMessage, false);
            ChatSDK.events().source().accept(NetworkEvent.messageAdded(newMessage));

            return new MessageSendRig(newMessage, thread).run();

        }).subscribeOn(RX.db());
    }

    // Moderation

    @Override
    public Completable mute(ThreadX thread) {
        return Completable.complete();
    }

    @Override
    public Completable unmute(ThreadX thread) {
        return Completable.complete();
    }

    @Override
    public boolean rolesEnabled(ThreadX thread) {
        return false;
    }

    @Override
    public boolean canChangeRole(ThreadX thread, User user) {
        return false;
    }

    @Override
    public String roleForUser(ThreadX thread, User user) {
        return null;
    }

    @Override
    public Completable setRole(String role, ThreadX thread, User user) {
        return Completable.error(ChatSDK.getException(R.string.feature_not_supported));
    }

    @Override
    public List<String> availableRoles(ThreadX thread, User user) {
        return new ArrayList<>();
    }

    @Override
    public Completable grantVoice(ThreadX thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public Completable revokeVoice(ThreadX thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public boolean hasVoice(ThreadX thread, User user) {
        return true;
    }

    @Override
    public boolean isBanned(ThreadX thread, User user) {
        return false;
    }

    @Override
    public boolean canChangeVoice(ThreadX thread, User user) {
        return false;
    }

    @Override
    public Completable grantModerator(ThreadX thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public Completable revokeModerator(ThreadX thread, User user) {
        return Completable.create(emitter -> {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
            emitter.onComplete();
        });
    }

    @Override
    public boolean canChangeModerator(ThreadX thread, User user) {
        return false;
    }

    @Override
    public boolean isModerator(ThreadX thread, User user) {
        return false;
    }

    @Override
    public List<String> localizeRoles(String... roles) {
        return localizeRoles(Arrays.asList(roles));
    }

    @Override
    public List<String> localizeRoles(List<String> roles) {
        List<String> localized = new ArrayList<>();
        for (String role: roles) {
            localized.add(localizeRole(role));
        }
        return localized;
    }

    public boolean canLeaveThread(ThreadX thread) {
        return thread.typeIs(ThreadType.PrivateGroup) && thread.containsUser(ChatSDK.currentUser());
    }

    @Override
    public boolean canEditThreadDetails(ThreadX thread) {
        return thread.getCreator().isMe() && !thread.typeIs(ThreadType.Private1to1);
    }

    @Override
    public Completable pushThreadMeta(ThreadX thread) {
        return Completable.complete();
    }

    @Override
    public Completable refreshRoles(ThreadX thread) {
        return Completable.complete();
    }

    @Override
    public boolean canRefreshRoles(ThreadX thread) {
        return false;
    }

    @Override
    public boolean isActive(ThreadX thread, User user) {
        return user.getIsOnline();
    }

    @Override
    public String readableEntityId(String entityID) {
        return entityID;
    }

}
