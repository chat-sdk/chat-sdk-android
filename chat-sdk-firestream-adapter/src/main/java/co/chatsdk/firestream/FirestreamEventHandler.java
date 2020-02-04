package co.chatsdk.firestream;

import java.util.Date;

import co.chatsdk.core.api.APIHelper;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.types.ReadStatus;

import co.chatsdk.firebase.FirebaseEventHandler;
import firestream.chat.events.Event;
import firestream.chat.filter.Filter;
import firestream.chat.interfaces.IChat;
import firestream.chat.events.EventType;
import firestream.chat.firebase.rx.DisposableMap;
import firestream.chat.namespace.Fire;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import co.chatsdk.core.dao.Thread;
import firestream.chat.namespace.FireStreamMessage;
import firestream.chat.types.RoleType;

public class FirestreamEventHandler extends FirebaseEventHandler implements Consumer<Throwable> {

    public FirestreamEventHandler() {
        Fire.stream().setMarkReceivedFilter(event -> {
            // Check if the message is read
            Message message = ChatSDK.db().fetchEntityWithEntityID(event.get().getId(), Message.class);
            return message == null || !message.readStatusForUser(ChatSDK.currentUser()).is(ReadStatus.read(), ReadStatus.delivered());
        });
    }

    protected DisposableMap dm = Fire.stream().getDisposableMap();

    @Override
    public void impl_currentUserOn(final String entityID) {
        super.impl_currentUserOn(entityID);
    }

    protected void threadsOn(User chatSDKUser) {

        dm.add(Fire.stream().getSendableEvents().getErrors().subscribe(throwable -> {
            throwable.printStackTrace();
        }));

        dm.add(Fire.stream().getChatEvents().subscribe(chatEvent -> {
            IChat chat = chatEvent.get();

            DisposableMap cdm = chat.getDisposableMap();

            if (chatEvent.typeIs(EventType.Added)) {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(chat.getId());
                if (thread == null) {
                    thread = ChatSDK.db().createEntity(Thread.class);
                    thread.setEntityID(chat.getId());
                    thread.setType(ThreadType.PrivateGroup);
                    thread.setCreationDate(new Date());
                    thread.update();

                    eventSource.onNext(NetworkEvent.threadAdded(thread));
                }

                final Thread finalThread = thread;

                // TODO: manage name image change
                cdm.add(chat.getNameChangeEvents().subscribe(s -> {
                    finalThread.setName(s);
                    eventSource.onNext(NetworkEvent.threadDetailsUpdated(finalThread));
                }, this));

                cdm.add(chat.getImageURLChangeEvents().subscribe(s -> {
                    finalThread.setImageUrl(s);
                    eventSource.onNext(NetworkEvent.threadDetailsUpdated(finalThread));
                }, this));

                cdm.add(chat.getUserEvents().subscribe(userEvent -> {
                    if (userEvent.typeIs(EventType.Added)) {
                        dm.put(chat.getId(), ChatSDK.core().getUserForEntityID(userEvent.get().getId()).subscribe(user -> {
                            if (userEvent.get().equalsRoleType(RoleType.owner())) {
                                finalThread.setCreator(user);
                            }
                            finalThread.addUser(user);
                            ChatSDK.core().userOn(user).subscribe(this);
                        }, this));
                    }
                    if (userEvent.typeIs(EventType.Removed)) {
                        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.get().getId());
                        finalThread.removeUser(user);
                    }
                }, this));

                // Handle new messages
                cdm.add(chat.getSendableEvents().getFireStreamMessages().subscribe(event -> {
                    handleMessageEvent(cdm, finalThread, event);
                }, this));

            }
            if (chatEvent.typeIs(EventType.Removed)) {
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(chat.getId());
                if (thread != null) {
                    eventSource.onNext(NetworkEvent.threadRemoved(thread));
                }
            }
        }, this));

        // Handle 1-to-1 messages
        dm.add(Fire.stream().getSendableEvents().getFireStreamMessages().subscribe(event -> {

            // Get the user
            dm.add(ChatSDK.core().getUserForEntityID(event.get().getFrom()).subscribe(user -> {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(event.get().getFrom());
                if (thread == null) {
                    thread = ChatSDK.db().createEntity(Thread.class);

                    thread.setEntityID(event.get().getFrom());
                    thread.setType(ThreadType.Private1to1);
                    thread.setCreationDate(new Date());
                    thread.setCreator(user);
                    thread.update();

                    // Add the sender
                    thread.addUsers(user, ChatSDK.currentUser());
                }

                handleMessageEvent(dm, thread, event);

            }, this));
        }));

        // Handle message deletion
        // We only do this if we are not deleting messages upon receipt, otherwise
        // the messages would just be deleted
        if (!Fire.internal().getConfig().deleteMessagesOnReceipt) {
            dm.add(Fire.stream().getSendableEvents()
                    .getSendables()
                    .filter(Filter.byEventType(EventType.Removed))
                    .subscribe(sendableEvent -> {
                String from = sendableEvent.get().getFrom();
                removeMessage(from, from);
            }, this));
        }

    }

    protected void removeMessage(String messageId, String threadId) {
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadId);
        if (thread != null) {
            Message message = thread.getMessageWithEntityID(messageId);
            if (message != null) {
                thread.removeMessage(message);
            }
        }
    }

    protected Disposable addMessage(FireStreamMessage mm, Thread thread) {
        if (!thread.containsMessageWithID(mm.getId())) {
            return FirestreamHelper.sendableToMessage(mm).subscribe(message -> {
                thread.addMessage(message);
            }, this);
        }
        return null;
    }

    @Override
    protected void contactsOn (User chatSDKUser) {
        dm.add(Fire.stream().getContactEvents().subscribe(userEvent -> {
            User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.get().getId());
            if (userEvent.typeIs(EventType.Added)) {
                dm.add(ChatSDK.contact().addContactLocal(contact, ConnectionType.Contact).doOnError(this).subscribe());
            }
            if (userEvent.typeIs(EventType.Removed)) {
                ChatSDK.contact().deleteContactLocal(contact, ConnectionType.Contact);
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

    public void handleMessageEvent(DisposableMap dm, Thread thread, Event<FireStreamMessage> event) {
        if (event.typeIs(EventType.Added)) {
            dm.add(addMessage(event.get(), thread));
        }
        if (event.typeIs(EventType.Modified)) {
            // Just update the date for now
            Message message = ChatSDK.db().fetchEntityWithEntityID(event.get().getId(), Message.class);
            if (message != null) {
                FirestreamHelper.copyToMessage(message, event.get());
            }
        }
        if (event.typeIs(EventType.Removed)) {
            if(!Fire.internal().getConfig().deleteMessagesOnReceipt) {
                removeMessage(event.get().getId(), thread.getEntityID());
            }
        }

    }
}
