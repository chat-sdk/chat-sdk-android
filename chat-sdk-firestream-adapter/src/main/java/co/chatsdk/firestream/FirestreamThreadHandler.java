package co.chatsdk.firestream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.ThreadMetaValue;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseThreadHandler;
import co.chatsdk.firebase.wrappers.MessageWrapper;
import co.chatsdk.firebase.wrappers.ThreadWrapper;
import co.chatsdk.firefly.R;
import firestream.chat.chat.Chat;
import firestream.chat.interfaces.IChat;
import firestream.chat.message.Sendable;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import firestream.chat.namespace.FireStreamUser;
import firestream.chat.types.RoleType;

public class FirestreamThreadHandler extends FirebaseThreadHandler {

    public Completable sendMessage(final Message message) {
        return Completable.defer(() -> {
            HashMap<String, Object> messageBody = new HashMap<>();

            messageBody.put(Keys.Type, message.getType());
            messageBody.put(Keys.Meta, message.getMetaValuesAsMap());

            if (message.getThread().getType() == ThreadType.Private1to1) {
                User otherUser = message.getThread().otherUser();
                return Fire.Stream.sendMessageWithBody(otherUser.getEntityID(), messageBody, message::setEntityID);
            } else {
                IChat chat = Fire.Stream.getChat(message.getThread().getEntityID());
                if (chat != null) {
                    return chat.sendMessageWithBody(messageBody, message::setEntityID);
                } else {
                    return Completable.error(new Throwable("Chat chat doesn't exist"));
                }
            }
        });
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users) {
        return createThread(name, users, ThreadType.None);
    }

    @Override
    public Single<Thread> createThread(final String name, final List<User> users, final int type) {
        return createThread(name, users, type, null);
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, int type, String entityID) {
        return createThread(name, users,type, entityID, null);
    }

    @Override
    public Single<Thread> createThread(String name, List<User> users, final int type, String entityID, String imageURL) {
        return Single.create((SingleOnSubscribe<Thread>) e -> {

        // Make sure that the current user isType in the list and
        // that they are not the first item
        ArrayList<User> allUsers = new ArrayList<>();
        allUsers.addAll(users);

        allUsers.remove(ChatSDK.currentUser());
        allUsers.add(ChatSDK.currentUser());

        Thread thread = ChatSDK.db().fetchThreadWithUsers(allUsers);
        if(thread != null) {
            e.onSuccess(thread);
        } else {

            thread = DaoCore.getEntityForClass(Thread.class);
            DaoCore.createEntity(thread);

            int threadType = type;

            if (type == ThreadType.None) {
                if (allUsers.size() == 2) {
                    threadType = ThreadType.Private1to1;
                } else {
                    threadType = ThreadType.PrivateGroup;
                }
            }

            if (threadType == ThreadType.Private1to1) {
                thread.addUsers(allUsers);
                thread.setEntityID(thread.otherUser().getEntityID());
            }

            thread.setCreator(ChatSDK.currentUser());

            thread.setCreationDate(new Date());
            thread.setType(threadType);
//            thread.addUsers(allUsers);

            if (name != null && !name.isEmpty()) {
                thread.setName(name);
            }
            if(imageURL != null && !imageURL.isEmpty()) {
                thread.setImageUrl(imageURL);
            }

            thread.update();

            Thread finalThread = thread;

            if(threadType == ThreadType.Private1to1) {
                if (allUsers.size() != 2) {
                    e.onError(new Throwable(Fire.privateApi().context().getString(R.string.error_private_chat_needs_two_members)));
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

                // We need to actually create the chat
                Fire.Stream.manage(Fire.Stream.createChat(name, imageURL, null, new ArrayList<>(usersToAdd)).subscribe((groupChat, throwable) -> {
                    if (throwable == null) {
                        finalThread.setEntityID(groupChat.getId());
                        e.onSuccess(finalThread);
                    } else {
                        e.onError(throwable);
                    }
                }));

            }
        }

        }).subscribeOn(Schedulers.single());
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
                return Completable.concat(changes);
            }
            return Completable.complete();
        });
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
        });
    }

    @Override
    public Completable deleteMessage(Message message) {
        return Completable.defer(() -> {
            // Get the thread
            Thread thread = message.getThread();
            if (thread.typeIs(ThreadType.Private1to1)) {
                Sendable sendable = Fire.stream().getSendable(message.getEntityID());
                if (sendable != null) {
                    return Fire.stream().deleteSendable(sendable);
                }
            }
            if (thread.typeIs(ThreadType.PrivateGroup)) {
                IChat chat = Fire.stream().getChat(thread.getEntityID());
                if (chat != null) {
                    Sendable sendable = chat.getSendable(message.getEntityID());
                    if (sendable != null) {
                        return chat.deleteSendable(sendable);
                    }
                }
            }
            return Completable.complete();
        });
    }


}
