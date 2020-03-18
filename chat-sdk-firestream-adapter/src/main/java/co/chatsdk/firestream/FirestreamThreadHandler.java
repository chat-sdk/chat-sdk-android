package co.chatsdk.firestream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firefly.R;
import firestream.chat.interfaces.IChat;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.Fire;
import firestream.chat.types.SendableType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.RoleType;

public class FirestreamThreadHandler extends AbstractThreadHandler {

    public Completable sendMessage(final Message message) {
        return Completable.defer(() -> {
            HashMap<String, Object> messageBody = new HashMap<>();

            messageBody.put(Keys.Type, message.getType());
            messageBody.put(Keys.Meta, message.getMetaValuesAsMap());

            if (message.getThread().getType() == ThreadType.Private1to1) {
                User otherUser = message.getThread().otherUser();
                return Fire.stream().sendMessageWithBody(otherUser.getEntityID(), messageBody, newId -> {
                    message.setEntityID(newId);
                    message.update();
                });
            } else {
                IChat chat = Fire.stream().getChat(message.getThread().getEntityID());
                if (chat != null) {
                    return chat.sendMessageWithBody(messageBody, newId -> {
                        message.setEntityID(newId);
                        message.update();
                    });
                } else {
                    // TODO: localise
                    return Completable.error(new Throwable("Chat chat doesn't exist"));
                }
            }
        });
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, final int type, String entityID, String imageURL) {
        return Single.create((SingleOnSubscribe<Thread>) e -> {

        // Make sure that the current user type in the list and
        // that they are not the first item
        ArrayList<User> allUsers = new ArrayList<>();
        allUsers.addAll(users);

        allUsers.remove(ChatSDK.currentUser());
        allUsers.add(ChatSDK.currentUser());

        Thread thread = ChatSDK.db().fetchThreadWithUsers(allUsers);
        if(thread != null) {
            e.onSuccess(thread);
        } else {
            thread = ChatSDK.db().createEntity(Thread.class);

            int threadType = type;

            if (type == ThreadType.None) {
                if (allUsers.size() == 2) {
                    threadType = ThreadType.Private1to1;
                } else {
                    threadType = ThreadType.PrivateGroup;
                }
            }

            thread.setCreator(ChatSDK.currentUser());

            thread.setCreationDate(new Date());
            thread.setType(threadType);

            if (name != null && !name.isEmpty()) {
                thread.setName(name);
            }
            if(imageURL != null && !imageURL.isEmpty()) {
                thread.setImageUrl(imageURL);
            }

            if (threadType == ThreadType.Private1to1) {
                thread.addUsers(allUsers);
                thread.setEntityID(thread.otherUser().getEntityID());
            }
            thread.update();

            if(threadType == ThreadType.Private1to1) {
                if (allUsers.size() != 2) {
                    e.onError(new Throwable(Fire.internal().context().getString(R.string.error_private_chat_needs_two_members)));
                } else {
                    e.onSuccess(thread);
                }
            } else {

                ArrayList<FireStreamUser> usersToAdd = new ArrayList<>();
                for (User u : allUsers) {
                    if (!u.isMe()) {
                        usersToAdd.add(new FireStreamUser(u.getEntityID(), RoleType.member()));
                    }
                }

                final Thread finalThread = thread;

                // We need to actually create the chat
                Fire.stream().manage(Fire.stream().createChat(name, imageURL, null, new ArrayList<>(usersToAdd)).subscribe((groupChat, throwable) -> {
                    if (throwable == null) {
                        finalThread.setEntityID(groupChat.getId());
                        e.onSuccess(finalThread);
                    } else {
                        e.onError(throwable);
                    }
                }));

            }
        }

        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable pushThread(Thread thread) {
        return Completable.defer(() -> {
            // Get the chat
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                // Check what has changed...
                ArrayList<Completable> changes = new ArrayList<>();

                if (!chat.getName().equals(thread.getName())) {
                    changes.add(chat.setName(thread.getName()));
                }
                if (!chat.getImageURL().equals(thread.getImageUrl())) {
                    changes.add(chat.setImageURL(thread.getImageUrl()));
                }

                boolean metaSame = true;

                HashMap<String, Object> chatMeta = chat.getCustomData();

                for (String key: chatMeta.keySet()) {
                    Object chatValue = chatMeta.get(key);
                    Object threadValue = thread.metaValueForKey(key);

                    if (chatValue == null || !chatValue.equals(threadValue)) {
                        chatMeta.put(key, threadValue);
                        metaSame = false;
                    }
                }
                if (!metaSame) {
                    changes.add(chat.setCustomData(chatMeta));
                }
                return Completable.concat(changes);
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable pushThreadMeta(Thread thread) {
        return Completable.defer(() -> {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                HashMap<String, Object> data = new HashMap<>();
                for (ThreadMetaValue v: thread.getMetaValues()) {
                    data.put(v.getKey(), v.getValue());
                }
                return chat.setCustomData(data);
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteMessage(final Message message) {
        return Completable.defer(() -> {
            // Get the thread
            Thread thread = message.getThread();
            if (thread.typeIs(ThreadType.Private1to1)) {
                return Fire.stream().deleteSendable(message.getEntityID());
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.deleteSendable(message.getEntityID());
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> {
            message.getThread().removeMessage(message);
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean deleteMessageEnabled(Message message) {
        if (message.getThread() != null && message.getThread().typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(message.getThread().getEntityID());
            if (chat != null && chat.hasPermission(RoleType.admin())) {
                return true;
            }
        }
        return message.getSender().isMe();
    }

    public boolean rolesEnabled(Thread thread, User user) {
        return thread.typeIs(ThreadType.Group) && !availableRoles(thread, user).isEmpty();
    }

    public String roleForUser(Thread thread, User user) {
        if (rolesEnabled(thread, user)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.getRoleType(new FireStreamUser(user.getEntityID())).stringValue();
            }
        }
        return null;
    }

    public List<String> availableRoles(Thread thread, User user) {
        if (rolesEnabled(thread, user)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return RoleType.rolesToStringValues(chat.getAvailableRoles(new FireStreamUser(user.getEntityID())));
            }
        }
        return new ArrayList<>();
    }

    public Completable setRole(String role, Thread thread, User user) {
        return Completable.defer(() -> {
            if (rolesEnabled(thread, user)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.setRole(new FireStreamUser(user.getEntityID()), RoleType.reverseMap().get(role));
                }
            }
            return Completable.error(new Throwable(ChatSDK.shared().getString(R.string.feature_not_supported)));
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean muteEnabled(Thread thread) {
        return true;
    }

    public Completable mute(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Private1to1)) {
                return Fire.stream().mute(new FireStreamUser(thread.getEntityID()));
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.mute();
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> thread.setMuted(true))
                .subscribeOn(Schedulers.io());
    }

    public Completable unmute(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Private1to1)) {
                return Fire.stream().unmute(new FireStreamUser(thread.getEntityID()));
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.unmute();
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> thread.setMuted(false)).subscribeOn(Schedulers.io());
    }

    public Completable removeUsersFromThread(final Thread thread, List<User> users) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    ArrayList<FireStreamUser> usersToRemove = new ArrayList<>();
                    for (User user: users) {
                        usersToRemove.add(new FireStreamUser(user.getEntityID()));
                    }
                    return chat.removeUsers(usersToRemove);
                }
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    public Completable addUsersToThread(final Thread thread, final List<User> users) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    ArrayList<FireStreamUser> usersToAdd = new ArrayList<>();
                    for (User user: users) {
                        usersToAdd.add(new FireStreamUser(user.getEntityID(), RoleType.member()));
                    }
                    return chat.addUsers(true, usersToAdd);
                }
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteThread(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Private1to1)) {
                return super.deleteThread(thread);
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.leave().concatWith(super.deleteThread(thread));
                }
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable leaveThread(Thread thread) {
        return null;
    }

    @Override
    public Completable joinThread(Thread thread) {
        return null;
    }

    public Single<List<Message>> loadMoreMessagesForThread(final Date fromDate, final Thread thread, boolean loadFromServer) {
        return super.loadMoreMessagesForThread(fromDate, thread, loadFromServer).flatMap(localMessages -> {

            // This function converts a list of sendables to a list of messages
            Function<List<Sendable>, SingleSource<List<Message>>> sendableToMessage = sendables -> Single.defer(() -> {
                // Convert the sendables to messages
                ArrayList<Single<Message>> singles = new ArrayList<>();
                for (Sendable sendable: sendables) {
                    if (sendable.isType(SendableType.message())) {
                        singles.add(FirestreamHelper.sendableToMessage(sendable));
                    }
                    if (sendable.isType(SendableType.deliveryReceipt())) {
                        if (ChatSDK.readReceipts() != null) {
                            ReadReceiptHandler handler = ChatSDK.readReceipts();
                            if (handler instanceof FirestreamReadReceiptHandler) {
                                ((FirestreamReadReceiptHandler) handler).handleReceipt(thread.getEntityID(), sendable.toDeliveryReceipt());
                            }
                        }
                    }
                }
                final ArrayList<Message> messages = new ArrayList<>();

                return Single.merge(singles).doOnNext(messages::add).ignoreElements().toSingle((Callable<List<Message>>) () -> {

                    ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                    mergedMessages.addAll(messages);

                    if (ChatSDK.encryption() != null) {
                        for (Message m : mergedMessages) {
                            ChatSDK.encryption().decrypt(m);
                        }
                    }

                    return mergedMessages;
                });
            });

            Date lastMessageDate = fromDate;
            if (localMessages.size() > 0) {
                // Don't get a duplicate of the previous message - get messages before that
                lastMessageDate = localMessages.get(localMessages.size() - 1).getDate().toDate();
            }

            if (thread.typeIs(ThreadType.Private1to1)) {
                return Fire.stream().loadMoreMessagesBefore(lastMessageDate, ChatSDK.config().messagesToLoadPerBatch).flatMap(sendableToMessage);
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.loadMoreMessagesBefore(lastMessageDate, ChatSDK.config().messagesToLoadPerBatch).flatMap(sendableToMessage);
                }
            }
            return Single.just(localMessages);
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean addUsersEnabled(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

    @Override
    public boolean removeUsersEnabled(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

}
