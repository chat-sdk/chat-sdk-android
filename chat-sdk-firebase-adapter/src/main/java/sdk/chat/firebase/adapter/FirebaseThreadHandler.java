package sdk.chat.firebase.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import sdk.chat.core.base.AbstractThreadHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Debug;
import sdk.chat.firebase.adapter.moderation.Permission;
import sdk.chat.firebase.adapter.wrappers.MessageWrapper;
import sdk.chat.firebase.adapter.wrappers.ThreadWrapper;
import sdk.guru.common.RX;
import sdk.guru.realtime.RXRealtime;

/**
 * Created by benjaminsmiley-andrews on 25/05/2017.
 */

public class FirebaseThreadHandler extends AbstractThreadHandler {

    @Override
    public Single<List<Message>> loadMoreMessagesAfter(Thread thread, @Nullable Date after, boolean loadFromServer) {
        return super.loadMoreMessagesAfter(thread, after, loadFromServer).flatMap(localMessages -> {
            // If we have messages
            // If we did load some messages locally, update the from date to the last of those messages
            Date finalAfterDate = localMessages.size() > 0 ? localMessages.get(0).getDate() : after;

            return new ThreadWrapper(thread).loadMoreMessagesAfter(finalAfterDate, 0).map((Function<List<Message>, List<Message>>) remoteMessages -> {

                ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                mergedMessages.addAll(remoteMessages);

                if (ChatSDK.encryption() != null) {
                    for (Message m : mergedMessages) {
                        ChatSDK.encryption().decrypt(m);
                    }
                }

                Debug.messageList(mergedMessages);

                return mergedMessages;
            });
        });
    }

    public Single<List<Message>> loadMoreMessagesBefore(final Thread thread, final Date fromDate, boolean loadFromServer) {
        return super.loadMoreMessagesBefore(thread, fromDate, loadFromServer).flatMap(localMessages -> {

            int messageToLoad = ChatSDK.config().messagesToLoadPerBatch;
            int localMessageSize = localMessages.size();

            if (localMessageSize < messageToLoad && loadFromServer) {
                // If we did load some messages locally, update the from date to the last of those messages
                Date finalFromDate = localMessageSize > 0 ? localMessages.get(localMessageSize-1).getDate() : fromDate;

                return new ThreadWrapper(thread).loadMoreMessagesBefore(finalFromDate, messageToLoad).map((Function<List<Message>, List<Message>>) remoteMessages -> {

                    ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                    mergedMessages.addAll(remoteMessages);

                    if (ChatSDK.encryption() != null) {
                        for (Message m : mergedMessages) {
                            ChatSDK.encryption().decrypt(m);
                        }
                    }

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
    public Completable addUsersToThread(final Thread thread, final List<User> users) {
        return new ThreadWrapper(thread).addUsers(users);
    }

    public Completable mute(Thread thread) {
        return Completable.defer(() -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            RXRealtime realtime = new RXRealtime();
            return realtime.set(threadUsersRef, true);
        });
    }

    public Completable unmute(Thread thread) {
        return Completable.defer(() -> {
            DatabaseReference threadUsersRef = FirebasePaths.threadUsersRef(thread.getEntityID()).child(ChatSDK.currentUserID()).child(Keys.Mute);
            RXRealtime realtime = new RXRealtime();
            return realtime.set(threadUsersRef, false);
        });
    }

    public Completable removeUsersFromThread(final Thread thread, List<User> users) {
        return new ThreadWrapper(thread).removeUsers(users);
    }

    public Completable pushThread(Thread thread) {
        return new ThreadWrapper(thread).push();
    }

    public Completable pushThreadMeta(Thread thread) {
        return new ThreadWrapper(thread).pushMeta();
    }

    @Override
    public boolean muteEnabled(Thread thread) {
        return true;
    }

    /**
     * Send a text,
     * The text need to have a owner thread attached to it or it cant be added.
     * If the destination thread is public the system will add the user to the text thread if needed.
     * The uploading to the server part can bee seen her {@see FirebaseCoreAdapter#PushMessageWithComplition}.
     */
    public Completable sendMessage(final Message message) {
        return new MessageWrapper(message).send().andThen(new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                pushForMessage(message);
                observer.onComplete();
            }
        });
    }

    public Single<Thread> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL, Map<String, Object> meta) {
        return Single.defer(() -> {

            // If the entity ID is set, see if the thread exists and return it if it does
            // TODO: Check this - what if for some reason the user isn't a member of this thread?
            if (entityID != null) {
                Thread t = ChatSDK.db().fetchThreadWithEntityID(entityID);
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
                Thread jointThread = null;

                for (User user : users) {
                    if (!user.equals(currentUser)) {
                        otherUser = user;
                        break;
                    }
                }

                // Check to see if a thread already exists with these
                // two users
                for(Thread thread : getThreads(ThreadType.Private1to1, ChatSDK.config().reuseDeleted1to1Threads, true)) {
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
                    jointThread.update();
                    return Single.just(jointThread);
                }
            }

            final Thread thread = ChatSDK.db().createEntity(Thread.class);

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

            thread.update();

            // Save the thread to the database.
            return new ThreadWrapper(thread).push()
                    .doOnError(throwable -> {
                        thread.delete();
                    })
                    .andThen(Completable.defer(() -> {
                        return ChatSDK.thread().addUsersToThread(thread, thread.getUsers());
                    }))
                    .toSingle(() -> thread);

        }).subscribeOn(RX.db());
    }

    public Completable deleteThread(Thread thread) {
        return Completable.defer(() -> {
            return new ThreadWrapper(thread).deleteThread();
        });
    }

    protected void pushForMessage(final Message message) {
        if (ChatSDK.push() != null && message.getThread().typeIs(ThreadType.Private)) {
            HashMap<String, Object> data = ChatSDK.push().pushDataForMessage(message);
            ChatSDK.push().sendPushNotification(data);
        }
    }

    public Completable deleteMessage(Message message) {
        return Completable.defer(() -> {
            if (message.getSender().isMe() && message.getMessageStatus().equals(MessageSendStatus.Sent) && !message.getMessageType().is(MessageType.System)) {
                return new MessageWrapper(message).delete();
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
        Thread thread = message.getThread();

        if (rolesEnabled(thread)) {
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

    public Completable leaveThread(Thread thread) {
        return Completable.defer(() -> new ThreadWrapper(thread).leave());
    }

    public Completable joinThread(Thread thread) {
        return null;
    }

    @Override
    public boolean rolesEnabled(Thread thread) {
        return thread.typeIs(ThreadType.Group);
    }

    @Override
    public boolean canChangeRole(Thread thread, User user) {
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
    public String roleForUser(Thread thread, @NonNull User user) {
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
    public Completable setRole(final String role, Thread thread, User user) {
        return Completable.defer(() -> {
//            role = Permission.fromLocalized(role);
            return new ThreadWrapper(thread).setPermission(user.getEntityID(), role);
        });
    }

    @Override
    public List<String> availableRoles(Thread thread, User user) {
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
    public List<String> localizeRoles(List<String> roles) {
        List<String> localized = new ArrayList<>();
        for (String role: roles) {
            localized.add(Permission.toLocalized(role));
        }
        return localized;
    }

    // Moderation
    @Override
    public Completable grantVoice(Thread thread, User user) {
        return Completable.complete();
    }

    @Override
    public Completable revokeVoice(Thread thread, User user) {
        return Completable.complete();
    }

    @Override
    public boolean hasVoice(Thread thread, User user) {
        if (thread.containsUser(user) || thread.typeIs(ThreadType.Public)) {
            String role = roleForUser(thread, user);
            return Permission.isOr(role, Permission.Owner, Permission.Admin, Permission.Member);
        }
        return false;
    }

    @Override
    public boolean canChangeVoice(Thread thread, User user) {
        return false;
    }

    @Override
    public Completable grantModerator(Thread thread, User user) {
        return Completable.complete();
    }

    @Override
    public Completable revokeModerator(Thread thread, User user) {
        return Completable.complete();
    }

    @Override
    public boolean canChangeModerator(Thread thread, User user) {
        return false;
    }

    @Override
    public boolean isModerator(Thread thread, User user) {
        return false;
    }

    @Override
    public boolean isBanned(Thread thread, User user) {
        if (thread.containsUser(user) || thread.typeIs(ThreadType.Public)) {
            String role = thread.getPermission(user.getEntityID());
            return Permission.isOr(role, Permission.Banned);
        }
        return false;
    }

    @Override
    public Message newMessage(int type, Thread thread) {
        Message message = super.newMessage(type, thread);
        // User Firebase to generate an ID
        String id = FirebasePaths.threadMessagesRef(thread.getEntityID()).push().getKey();
        message.setEntityID(id);
        message.update();
        return message;
    }

}
