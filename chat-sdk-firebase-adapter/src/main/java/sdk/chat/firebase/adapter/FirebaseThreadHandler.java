package sdk.chat.firebase.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import sdk.chat.core.base.AbstractThreadHandler;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Debug;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.guru.common.RX;
import sdk.guru.realtime.RXRealtime;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class FirebaseThreadHandler extends AbstractThreadHandler {

    @Override
    public Single<List<Message>> loadMoreMessagesAfter(ThreadX thread, @Nullable Date after, boolean loadFromServer) {
        return super.loadMoreMessagesAfter(thread, after, loadFromServer).flatMap(localMessages -> {
            // If we have messages
            // If we did load some messages locally, update the from date to the last of those messages
            Date finalAfterDate = localMessages.size() > 0 ? localMessages.get(0).getDate() : after;

            return FirebaseModule.config().provider.threadWrapper(thread).loadMoreMessagesAfter(finalAfterDate, 0).map((Function<List<Message>, List<Message>>) remoteMessages -> {

                ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                mergedMessages.addAll(remoteMessages);

//                if (ChatSDK.encryption() != null) {
//                    for (Message m : mergedMessages) {
//                        ChatSDK.encryption().decrypt(m);
//                    }
//                }

                Debug.messageList(mergedMessages);

                return mergedMessages;
            });
        });
    }

    public Single<List<Message>> loadMoreMessagesBefore(final ThreadX thread, final Date fromDate, boolean loadFromServer) {
        return super.loadMoreMessagesBefore(thread, fromDate, loadFromServer).flatMap(localMessages -> {

            int messageToLoad = ChatSDK.config().messagesToLoadPerBatch;
            int localMessageSize = localMessages.size();

            if (localMessageSize < messageToLoad && loadFromServer) {
                // If we did load some messages locally, update the from date to the last of those messages
                Date finalFromDate = localMessageSize > 0 ? localMessages.get(localMessageSize-1).getDate() : fromDate;

                return FirebaseModule.config().provider.threadWrapper(thread).loadMoreMessagesBefore(finalFromDate, messageToLoad).map((Function<List<Message>, List<Message>>) remoteMessages -> {

                    ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                    mergedMessages.addAll(remoteMessages);

//                    if (ChatSDK.encryption() != null) {
//                        for (Message m : mergedMessages) {
//                            ChatSDK.encryption().decrypt(m);
//                        }
//                    }

                    Debug.messageList(mergedMessages);

                    return mergedMessages;
                });
            }
            return Single.just(localMessages);
        });
    }

    /**
     * Add given users list to the given thread.
     * The RepetitiveCompletionListenerWithError will notify by his "onItem" method for each user that was successfully added.
     * In the "onItemFailed" you can get all users that the system could not add to the server.
     * When all users are added the system will call the "onDone" method.
     **/
    public Completable addUsersToThread(final ThreadX thread, final List<User> users) {
        return FirebaseModule.config().provider.threadWrapper(thread).addUsers(users);
    }

    @Override
    public boolean canAddUsersToThread(ThreadX thread) {
        if (thread.typeIs(ThreadType.PrivateGroup)) {
            String role = roleForUser(thread, ChatSDK.currentUser());
            return Permission.isOr(role, Permission.Owner, Permission.Admin);
        }
        return false;
    }

    public Completable mute(ThreadX thread) {
        return Completable.defer(() -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            RXRealtime realtime = new RXRealtime();
            return realtime.set(threadUsersRef, true);
        });
    }

    public Completable unmute(ThreadX thread) {
        return Completable.defer(() -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            RXRealtime realtime = new RXRealtime();
            return realtime.set(threadUsersRef, false);
        });
    }

    public Completable removeUsersFromThread(final ThreadX thread, List<User> users) {
        return FirebaseModule.config().provider.threadWrapper(thread).removeUsers(users);
    }

    public Completable pushThread(ThreadX thread) {
        return FirebaseModule.config().provider.threadWrapper(thread).push();
    }

    public Completable pushThreadMeta(ThreadX thread) {
        return FirebaseModule.config().provider.threadWrapper(thread).pushMeta();
    }

    @Override
    public boolean muteEnabled(ThreadX thread) {
        return true;
    }

    /**
     * Send a text,
     * The text need to have a owner thread attached to it or it cant be added.
     * If the destination thread is public the system will add the user to the text thread if needed.
     * The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.
     */
    public Completable sendMessage(final Message message) {
        return FirebaseModule.config().provider.messageWrapper(message).send().andThen(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                pushForMessage(message);
                observer.onComplete();
            }
        });
    }

    public Single<ThreadX> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL, Map<String, Object> meta) {
        return Single.defer(() -> {

            // If the entity ID is set, see if the thread exists and return it if it does
            // TODO: Check this - what if for some reason the user isn't a member of this thread?
            if (entityID != null) {
                ThreadX t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    return Single.just(t);
                }
            }
            ArrayList<User> users = new ArrayList<>(theUsers);
            User currentUser = ChatSDK.currentUser();

            if (!users.contains(currentUser)) {
                users.add(currentUser);
            }

            if (users.size() == 2 && (type == ThreadType.None || ThreadType.is(type, ThreadType.Private1to1))) {

                User otherUser = null;
                ThreadX jointThread = null;

                for (User user : users) {
                    if (!user.equals(currentUser)) {
                        otherUser = user;
                        break;
                    }
                }

                // Check to see if a thread already exists with these
                // two users
                for(ThreadX thread : getThreads(ThreadType.Private1to1, ChatSDK.config().reuseDeleted1to1Threads, true)) {
                    if(thread.getUsers().size() == 2 &&
                            thread.containsUser(currentUser) &&
                            thread.containsUser(otherUser))
                    {
                        jointThread = thread;
                        break;
                    }
                }

                if(jointThread != null) {
                    jointThread.setDeleted(false);
                    ChatSDK.db().update(jointThread);
                    return Single.just(jointThread);
                }
            }

            // TODO: Thread
            final ThreadX thread = ChatSDK.db().createEntity(ThreadX.class);

//            ChatSDK.db().getDaoCore().getDaoSession().runInTx(() -> {

                thread.setEntityID(entityID);
                thread.setCreator(currentUser);
                thread.setCreationDate(new Date());

                if (name != null) {
                    thread.setName(name, false);
                }

                if (imageURL != null) {
                    thread.setImageUrl(imageURL, false);
                }

                thread.addUsers(users);

                if (meta != null) {
                    for (String key: meta.keySet()) {
                        thread.setMetaValue(key, meta.get(key), false);
                    }
                }

                if (type != -1) {
                    thread.setType(type);
                } else {
                    thread.setType(users.size() == 2 ? ThreadType.Private1to1 : ThreadType.PrivateGroup);
                }

                ChatSDK.db().update(thread);
//            });

            ThreadWrapper wrapper = FirebaseModule.config().provider.threadWrapper(thread);

            // Save the thread to the database.
            return wrapper.push()
                    .doOnError(throwable -> {
                        thread.cascadeDelete();
                    })
                    .andThen(Completable.defer(() -> {
                        return ChatSDK.thread().addUsersToThread(thread, thread.getUsers());
                    }))
                    .andThen(wrapper.setPermission(ChatSDK.currentUserID(), Permission.Owner))
                    .toSingle(() -> thread);

        }).subscribeOn(RX.db());
    }

    public Completable deleteThread(ThreadX thread) {
        return Completable.defer(() -> {
            return FirebaseModule.config().provider.threadWrapper(thread).deleteThread()
                    // Added to make sure thread removed (bug report from Dcom)
                    .andThen(super.deleteThread(thread));
        });
    }

    protected void pushForMessage(final Message message) {
        if (ChatSDK.push() != null && message.getThread().typeIs(ThreadType.Private)) {
            Map<String, Object> data = ChatSDK.push().pushDataForMessage(message);
            ChatSDK.push().sendPushNotification(data);
        }
    }

    public Completable deleteMessage(Message message) {
        return Completable.defer(() -> {
            if (message.getSender().isMe() && message.getMessageStatus().equals(MessageSendStatus.Sent) && !message.getMessageType().is(MessageType.System)) {
                // If possible delete the files associated with this message

                List<CachedFile> files = ChatSDK.db().fetchFilesWithIdentifier(message.getEntityID());
                for (CachedFile file: files) {
                    if (file.getRemotePath() != null) {
                        ChatSDK.upload().deleteFile(file.getRemotePath()).subscribe();
                    }
                    ChatSDK.db().delete(file);
                }

                // TODO: Can we do this with cached files?
//                MessagePayload payload = ChatSDK.getMessagePayload(message);
//                if (payload != null) {
//                    List<String> paths = payload.remoteURLs();
//                    for (String path: paths) {
//                        ChatSDK.upload().deleteFile(path).subscribe();
//                    }
//                }

                return FirebaseModule.config().provider.messageWrapper(message).delete();
            }
            message.getThread().removeMessage(message);
            return Completable.complete();
        });
    }

    @Override
    public boolean canDeleteMessage(Message message) {

        // We do it this way because otherwise when we exceed the number of messages,
        // This event is triggered as the messages go out of scope
        if (message.getDate().getTime() < message.getThread().getCanDeleteMessagesFrom().getTime()) {
            return false;
        }

        User currentUser = ChatSDK.currentUser();
        ThreadX thread = message.getThread();

        if (rolesEnabled(thread) && !thread.typeIs(ThreadType.Public)) {
            String role = roleForUser(thread, currentUser);
            int level = Permission.level(role);

            if (level > Permission.level(Permission.Member)) {
                return true;
            }
            if (level < Permission.level(Permission.Member)) {
                return false;
            }
        }

        if (isModerator(thread, currentUser)) {
            return true;
        }

        if (!hasVoice(thread, currentUser)) {
            return false;
        }

        if (message.getSender().isMe()) {
            return true;
        }

        return false;
    }

    public Completable leaveThread(ThreadX thread) {
        return Completable.defer(() -> FirebaseModule.config().provider.threadWrapper(thread).leave());
    }

    @Override
    public boolean canLeaveThread(ThreadX thread) {
        if (thread.typeIs(ThreadType.PrivateGroup)) {
            // Get the link
            String role = roleForUser(thread, ChatSDK.currentUser());
            return Permission.isOr(role, Permission.Owner, Permission.Admin, Permission.Member, Permission.Watcher);
        }
        return false;
    }

    public Completable joinThread(ThreadX thread) {
        return null;
    }

    @Override
    public boolean canJoinThread(ThreadX thread) {
        return false;
    }

    @Override
    public boolean rolesEnabled(ThreadX thread) {
        return thread.typeIs(ThreadType.Group);
    }

    @Override
    public boolean canChangeRole(ThreadX thread, User user) {
        if (!ChatSDK.config().rolesEnabled || !thread.typeIs(ThreadType.Group)) {
            return false;
        }

        String myRole = roleForUser(thread, ChatSDK.currentUser());
        String role = roleForUser(thread, user);

        // We need to have a higher permission level than them
        if (Permission.level(myRole) > Permission.level(role)) {
            return Permission.isOr(myRole, Permission.Owner, Permission.Admin);
        }
        return false;
    }

    @Override
    public String roleForUser(ThreadX thread, @NonNull User user) {
        UserThreadLink link = thread.getUserThreadLink(user.getId());
        if (link != null && link.hasLeft()) {
            return Permission.None;
        }
        if (user.equalsEntity(thread.getCreator())) {
            return Permission.Owner;
        }
        String role = thread.getPermission(user.getEntityID());
        if (role == null) {
            role = Permission.Member;
        }
        return role;
    }

    @Override
    public Completable setRole(final String role, ThreadX thread, User user) {
        return Completable.defer(() -> {
//            role = Permission.fromLocalized(role);
            return FirebaseModule.config().provider.threadWrapper(thread).setPermission(user.getEntityID(), role);
        });
    }

    @Override
    public List<String> availableRoles(ThreadX thread, User user) {
        List<String> roles = new ArrayList<>();

        String myRole = roleForUser(thread, ChatSDK.currentUser());

        for (String role: Permission.all()) {
            if (Permission.level(myRole) > Permission.level(role)) {
                roles.add(role);
            }
        }

        // In public chats it doesn't make sense to ban a user because
        // the data is public. They can be made a watcher instead
        if (thread.typeIs(ThreadType.Public)) {
            roles.remove(Permission.Banned);
        }

        return roles;
    }

    @Override
    public String localizeRole(String role) {
        return Permission.toLocalized(role);
    }

    // Moderation
    @Override
    public Completable grantVoice(ThreadX thread, User user) {
        return Completable.complete();
    }

    @Override
    public Completable revokeVoice(ThreadX thread, User user) {
        return Completable.complete();
    }

    @Override
    public boolean hasVoice(ThreadX thread, User user) {
        if (thread.containsUser(user) || thread.typeIs(ThreadType.Public)) {
            String role = roleForUser(thread, user);
            return Permission.isOr(role, Permission.Owner, Permission.Admin, Permission.Member);
        }
        return false;
    }

    @Override
    public boolean canChangeVoice(ThreadX thread, User user) {
        return false;
    }

    @Override
    public Completable grantModerator(ThreadX thread, User user) {
        return Completable.complete();
    }

    @Override
    public Completable revokeModerator(ThreadX thread, User user) {
        return Completable.complete();
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
    public boolean isBanned(ThreadX thread, User user) {
        if (thread.containsUser(user) || thread.typeIs(ThreadType.Public)) {
            String role = thread.getPermission(user);
            return Permission.isOr(role, Permission.Banned);
        }
        return false;
    }

    @Override
    public String generateNewMessageID(ThreadX thread) {
        // User Firebase to generate an ID
        return FirebasePaths.threadMessagesRef(thread.getEntityID()).push().getKey();
    }

    @Override
    public Message newMessage(int type, ThreadX thread, boolean notify) {
        Message message = super.newMessage(type, thread, notify);
        ChatSDK.db().update(message);
        return message;
    }

    @Override
    public boolean canDestroy(ThreadX thread) {
        return false;
    }

    @Override
    public Completable destroy(ThreadX thread) {
        return Completable.complete();
    }

    @Override
    public boolean canEditThreadDetails(ThreadX thread) {
        if (thread.typeIs(ThreadType.Group)) {
            String role = roleForUser(thread, ChatSDK.currentUser());
            return Permission.isOr(role, Permission.Owner, Permission.Admin);
        }
        return false;
    }

    @Override
    public boolean canRemoveUserFromThread(ThreadX thread, User user) {
        if (thread.typeIs(ThreadType.PrivateGroup)) {
            String myRole = roleForUser(thread, ChatSDK.currentUser());
            String role = roleForUser(thread, user);
            return Permission.isOr(myRole, Permission.Owner, Permission.Admin) && Permission.isOr(role, Permission.Member, Permission.Watcher, Permission.Banned);
        }
        return false;
    }
}
