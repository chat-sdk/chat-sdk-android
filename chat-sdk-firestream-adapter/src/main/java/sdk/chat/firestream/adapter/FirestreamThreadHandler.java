package sdk.chat.firestream.adapter;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import firestream.chat.interfaces.IChat;
import firestream.chat.message.Body;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.Fire;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.RoleType;
import firestream.chat.types.SendableType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
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
import sdk.guru.common.RX;

public class FirestreamThreadHandler extends AbstractThreadHandler {

    public Completable sendMessage(final Message message) {
        return Completable.defer(() -> {

            Body body = new Body();
            body.setType(String.valueOf(message.getType()));
            body.put(Keys.Meta, message.getMetaValuesAsMap());

            if (message.getThread().getType() == ThreadType.Private1to1) {
                User otherUser = message.getThread().otherUser();
                return Fire.stream().sendMessageWithBody(otherUser.getEntityID(), body, newId -> {
                    message.setEntityID(newId);
                    message.update();
                });
            } else {
                IChat chat = Fire.stream().getChat(message.getThread().getEntityID());
                if (chat != null) {
                    return chat.sendMessageWithBody(body, newId -> {
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
    public Single<Thread> createThread(String name, List<User> users, final int type, String entityID, String imageURL, Map<String, Object> meta) {
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
                Fire.stream().manage(Fire.stream().createChat(name, imageURL, meta, new ArrayList<>(usersToAdd)).subscribe((groupChat, throwable) -> {
                    if (throwable == null) {
                        finalThread.setEntityID(groupChat.getId());
                        e.onSuccess(finalThread);
                    } else {
                        e.onError(throwable);
                    }
                }));

            }
        }

        }).subscribeOn(RX.computation());
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

                Map<String, Object> chatMeta = chat.getCustomData();

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
        }).subscribeOn(RX.pool());
    }

    @Override
    public Completable deleteMessage(final Message message) {
        return Completable.defer(() -> {
            if ((message.getSender().isMe() && message.getMessageStatus().equals(MessageSendStatus.Sent))
                    || !message.getSender().isMe()
                    || !message.getMessageType().is(MessageType.System)) {

                // Get the thread
                Thread thread = message.getThread();
                if (thread.typeIs(ThreadType.Private1to1)) {
                    User otherUser = thread.otherUser();
                    return Fire.stream().deleteSendable(otherUser.getEntityID(), message.getEntityID());
                }
                if (thread.typeIs(ThreadType.Group)) {
                    IChat chat = Fire.stream().getChat(thread.getEntityID());
                    if (chat != null) {
                        return chat.deleteSendable(message.getEntityID());
                    }
                }
            }
            return Completable.complete();
        }).doOnComplete(() -> {
            message.getThread().removeMessage(message);
        });
    }

    @Override
    public boolean canDeleteMessage(Message message) {
        if (message.getThread() != null && message.getThread().typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(message.getThread().getEntityID());

            if (chat != null && (chat.hasPermission(RoleType.admin()) || message.getSender().isMe())) {
                return chat.getSendable(message.getEntityID()) != null;
            }
        }
        // Only messages that were added this session can be deleted
        return (message.getSender().isMe() && message.getMessageStatus() == MessageSendStatus.Failed
                || !Fire.stream().getConfig().deleteMessagesOnReceipt
                || message.getMessageType().is(MessageType.System));
    }

    @Override
    public boolean rolesEnabled(Thread thread) {
        return ChatSDK.config().rolesEnabled && thread.typeIs(ThreadType.Group);
    }

    public String roleForUser(Thread thread, User user) {
        if (rolesEnabled(thread)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.getRoleType(new FireStreamUser(user.getEntityID())).get();
            }
        }
        return null;
    }

    public List<String> availableRoles(Thread thread, User user) {
        if (rolesEnabled(thread)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return RoleType.rolesToStringValues(chat.getAvailableRoles(new FireStreamUser(user.getEntityID())));
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String localizeRole(String role) {
        return new RoleType(role).localized();
    }

    public Completable setRole(String role, Thread thread, User user) {
        return Completable.defer(() -> {
            if (rolesEnabled(thread)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.setRole(new FireStreamUser(user.getEntityID()), RoleType.reverseMap().get(role));
                }
            }
            return Completable.error(ChatSDK.getException(R.string.feature_not_supported));
        });
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
        }).doOnComplete(() -> thread.setMuted(true));
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
        }).doOnComplete(() -> thread.setMuted(false));
    }

    @Override
    public boolean canDestroy(Thread thread) {
        return false;
    }

    @Override
    public Completable destroy(Thread thread) {
        return null;
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
        });
    }

    public Completable addUsersToThread(final Thread thread, final List<User> users, @Nullable List<String> permissions) {
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
        });
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
        });
    }

    @Override
    public boolean canLeaveThread(Thread thread) {
        if (super.canLeaveThread(thread)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            return chat != null;
        }
        return false;
    }

    @Override
    public Completable leaveThread(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.leave();
                }
            }
            return Completable.complete();
        });
    }

    @Override
    public Completable joinThread(Thread thread) {
        return Completable.complete();
    }

    @Override
    public boolean canJoinThread(Thread thread) {
        return false;
    }

    @Override
    public Single<List<Message>> loadMoreMessagesBefore(final Thread thread, final Date olderThan, boolean loadFromServer) {
        return super.loadMoreMessagesBefore(thread, olderThan, loadFromServer).flatMap(localMessages -> {

            // This function converts a list of sendables to a list of messages
            Function<List<Sendable>, SingleSource<List<Message>>> sendableToMessage = sendables -> Single.defer(() -> {
                // Convert the sendables to messages
                ArrayList<Single<Message>> singles = new ArrayList<>();
                for (Sendable sendable: sendables) {
                    if (sendable.isType(SendableType.message())) {
                        singles.add(FirestreamHelper.sendableToMessage(thread, sendable, false));
                    }
                    if (sendable.isType(SendableType.deliveryReceipt())) {
                        if (ChatSDK.readReceipts() != null) {
                            // TODO: Check this
//                            DeliveryReceipt receipt = sendable.toDeliveryReceipt();
//                            ReadReceiptHandler handler = ChatSDK.readReceipts();
//                            if (handler instanceof FirestreamReadReceiptHandler) {
//                                ((FirestreamReadReceiptHandler) handler).handleReceipt(thread.getEntityID(), sendable.toDeliveryReceipt());
//                            }
                        }
                    }
                }
                final ArrayList<Message> messages = new ArrayList<>();

                return Single.concat(singles).doOnNext(messages::add).ignoreElements().toSingle((Callable<List<Message>>) () -> {

                    ArrayList<Message> mergedMessages = new ArrayList<>(localMessages);
                    mergedMessages.addAll(messages);

//                    if (ChatSDK.encryption() != null) {
//                        for (Message m : mergedMessages) {
//                            ChatSDK.encryption().decrypt(m);
//                        }
//                    }

                    return mergedMessages;
                });
            });

            Date lastMessageDate = olderThan;
            if (localMessages.size() > 0) {
                // Don't get a duplicate of the previous message - get messages before that
                lastMessageDate = localMessages.get(localMessages.size() - 1).getDate();
            }

            if (thread.typeIs(ThreadType.Private1to1)) {
                return Fire.stream().loadMoreMessagesBefore(lastMessageDate, ChatSDK.config().messagesToLoadPerBatch, true).flatMap(sendableToMessage);
            }
            if (thread.typeIs(ThreadType.Group)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    return chat.loadMoreMessagesBefore(lastMessageDate, ChatSDK.config().messagesToLoadPerBatch, true).flatMap(sendableToMessage);
                }
            }
            return Single.just(localMessages);
        }).subscribeOn(RX.io());
    }

    @Override
    public boolean canAddUsersToThread(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

    @Override
    public boolean canRemoveUsersFromThread(Thread thread, List<User> users) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

    @Override
    public boolean canEditThreadDetails(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

    @Override
    public boolean canChangeRole(Thread thread, User user) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.admin());
            }
        }
        return false;
    }

    @Override
    public boolean isBanned(Thread thread, User user) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.banned());
            }
        }
        return false;
    }


    @Override
    public boolean hasVoice(Thread thread, User user) {
        if (thread.typeIs(ThreadType.Group)) {
            IChat chat = Fire.stream().getChat(thread.getEntityID());
            if (chat != null) {
                return chat.hasPermission(RoleType.member());
            }
        }
        return thread.typeIs(ThreadType.Private1to1);
    }

}
