package co.chatsdk.firestore;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashMap;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.firebase.FirebaseEventHandler;
import io.reactivex.functions.Consumer;
import sdk.chat.micro.Fireflyy;
import co.chatsdk.core.dao.Thread;
import sdk.chat.micro.chat.Chat;
import sdk.chat.micro.events.EventType;
import sdk.chat.micro.namespace.Fire;
import sdk.chat.micro.namespace.MicroMessage;
import sdk.chat.micro.types.RoleType;

public class FirestoreEventHandler extends FirebaseEventHandler implements Consumer<Throwable> {

    @Override
    public void impl_currentUserOn(final String entityID) {
        super.impl_currentUserOn(entityID);
    }

    protected void threadsOn(User chatSDKUser) {

        disposableList.add(Fire.flyy.getChatEvents().subscribe(chatEvent -> {
            if (chatEvent.type == EventType.Added) {
                Chat chat = chatEvent.chat;

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(chat.getId());
                if (thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(chat.getId());
                    thread.setType(ThreadType.PrivateGroup);
                    thread.setCreationDate(new Date());

                    eventSource.onNext(NetworkEvent.threadAdded(thread));

                }

                final Thread finalThread = thread;

                // TODO: handle name image change
                disposableList.add(chat.getNameStream().subscribe(s -> {
                    finalThread.setName(s);
                    eventSource.onNext(NetworkEvent.threadDetailsUpdated(finalThread));
                }));

                disposableList.add(chat.getAvatarURLStream().subscribe(s -> {
                    finalThread.setImageUrl(s);
                    eventSource.onNext(NetworkEvent.threadDetailsUpdated(finalThread));
                }));

                disposableList.add(chat.getUserEventStream().subscribe(userEvent -> {
                    if (userEvent.type == EventType.Added) {
                        disposableList.add(APIHelper.fetchRemoteUser(userEvent.user.id).subscribe(user -> {
                            if (userEvent.getMicroUser().roleType.equals(RoleType.owner())) {
                                finalThread.setCreator(user);
                            }
                            finalThread.addUser(user);
                            ChatSDK.core().userOn(user).subscribe(new CrashReportingCompletableObserver());
                            eventSource.onNext(NetworkEvent.threadUsersChanged(finalThread, user));
                        }, this));
                    }
                    if (userEvent.type == EventType.Removed) {
                        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.user.id);
                        finalThread.removeUser(user);
                        eventSource.onNext(NetworkEvent.threadUsersChanged(finalThread, user));
                    }
                }));

                disposableList.add(chat.getStream().getMicroMessages().subscribe(message -> {
                    handleMessageForThread(message, finalThread);
                }));
            }
        }));

        disposableList.add(Fire.flyy.getStream().getMicroMessages().subscribe(message -> {
            // Get the user
            disposableList.add(APIHelper.fetchRemoteUser(message.from).subscribe(user -> {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(message.from);
                if (thread == null) {
                    thread = DaoCore.getEntityForClass(Thread.class);
                    DaoCore.createEntity(thread);
                    thread.setEntityID(message.from);
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    thread.setCreator(user);

                    // Add the sender
                    thread.addUsers(user, ChatSDK.currentUser());
                }

                handleMessageForThread(message, thread);
            }));
        }));
    }

    protected void handleMessageForThread(MicroMessage mm, Thread thread) {
        disposableList.add(APIHelper.fetchRemoteUser(mm.from).subscribe(user -> {

            Message message = ChatSDK.db().createEntity(Message.class);

            message.setSender(user);
            message.setMessageStatus(MessageSendStatus.Delivered);
            message.setDate(new DateTime(mm.date));
            message.setEntityID(mm.id);

            HashMap<String, Object> body = mm.getBody();

            Object metaObject = body.get(Keys.Meta);
            if (metaObject instanceof HashMap) {
                HashMap<String, Object> meta = new HashMap<>((HashMap) metaObject);
                message.setMetaValues(meta);
            }

            Object typeObject = body.get(Keys.Type);

            if (typeObject instanceof Long) {
                Integer type = ((Long) typeObject).intValue();
                message.setType(type);
            }
            if (typeObject instanceof Integer) {
                Integer type = (Integer) typeObject;
                message.setType(type);
            }

            thread.addMessage(message);

            eventSource.onNext(NetworkEvent.messageAdded(thread, message));
            }, this));
    }

    @Override
    protected void contactsOn (User chatSDKUser) {
        disposableList.add(Fire.flyy.getContactEvents().subscribe(userEvent -> {
            User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.user.id);
            if (userEvent.type == EventType.Added) {
                disposableList.add(ChatSDK.core().userOn(contact).subscribe(() -> {
                    ChatSDK.contact().addContactLocal(contact, ConnectionType.Contact);
                    eventSource.onNext(NetworkEvent.contactAdded(contact));
                }, this));
            }
            if (userEvent.type == EventType.Removed) {
                ChatSDK.contact().deleteContactLocal(contact, ConnectionType.Contact);
                eventSource.onNext(NetworkEvent.contactDeleted(contact));
            }
        }, this));
    }

    @Override
    protected void publicThreadsOn (User user) {
    }

    @Override
    public void impl_currentUserOff(final String entityID) {
        super.impl_currentUserOff(entityID);
    }

    protected void threadsOff (User user) {

    }

    protected void publicThreadsOff (User user) {

    }

    protected void contactsOff (User user) {

    }


    @Override
    public void accept(Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }
}
