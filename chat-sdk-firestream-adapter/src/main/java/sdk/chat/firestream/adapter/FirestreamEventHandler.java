package sdk.chat.firestream.adapter;

import java.util.Date;

import sdk.chat.firebase.adapter.FirebaseEventHandler;
import firestream.chat.filter.Filter;
import firestream.chat.interfaces.IChat;
import firestream.chat.namespace.Fire;
import firestream.chat.namespace.FireStreamMessage;
import firestream.chat.types.RoleType;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.Event;
import sdk.guru.common.EventType;

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

            if (chatEvent.isAdded()) {

                // Get the thread
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(chat.getId());
                if (thread == null) {
                    thread = ChatSDK.db().createEntity(Thread.class);
                    thread.setEntityID(chat.getId());
                    thread.setType(ThreadType.PrivateGroup);
                    thread.setCreationDate(new Date());
                    thread.update();

                    eventSource.accept(NetworkEvent.threadAdded(thread));
                }

                final Thread finalThread = thread;

                // TODO: manage name image change
                cdm.add(chat.getNameChangeEvents().subscribe(s -> {
                    finalThread.setName(s);
                    eventSource.accept(NetworkEvent.threadMetaUpdated(finalThread));
                }, this));

                cdm.add(chat.getImageURLChangeEvents().subscribe(s -> {
                    finalThread.setImageUrl(s);
                    eventSource.accept(NetworkEvent.threadMetaUpdated(finalThread));
                }, this));

                cdm.add(chat.getUserEvents().subscribe(userEvent -> {
                    if (userEvent.isAdded()) {
                        dm.put(chat.getId(), ChatSDK.core().getUserForEntityID(userEvent.get().getId()).subscribe(user -> {
                            if (userEvent.get().equalsRoleType(RoleType.owner())) {
                                finalThread.setCreator(user);
                            }
                            finalThread.addUser(user);
                            ChatSDK.core().userOn(user).subscribe(this);
                        }, this));
                    }
                    if (userEvent.isRemoved()) {
                        User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.get().getId());
                        finalThread.removeUser(user);
                    }
                    // Capture role change events
                    if (userEvent.isModified()) {
                        RoleType role = chat.getRoleType(userEvent.get());
                        finalThread.setPermission(userEvent.get().getId(), ChatSDK.thread().localizeRole(role.get()));
                    }
                }, this));

                // Handle new messages
                cdm.add(chat.getSendableEvents().getFireStreamMessages().subscribe(event -> {
                    handleMessageEvent(cdm, finalThread, event);
                }, this));

            }
            if (chatEvent.isRemoved()) {
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(chat.getId());
                if (thread != null) {
                    eventSource.accept(NetworkEvent.threadRemoved(thread));
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
        if (!Fire.stream().getConfig().deleteMessagesOnReceipt) {
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
            return FirestreamHelper.sendableToMessage(thread, mm, true).subscribe(message -> {
                thread.setDeleted(false);
            }, this);
        }
        return null;
    }

    @Override
    protected void contactsOn(User chatSDKUser) {
        dm.add(Fire.stream().getContactEvents().pastAndNewEvents().subscribe(userEvent -> {
            User contact = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEvent.get().getId());
            if (userEvent.isAdded()) {
                ChatSDK.contact().addContactLocal(contact, ConnectionType.Contact).subscribe(this);
            }
            if (userEvent.isRemoved()) {
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
    public void accept(Throwable throwable) {
        throwable.printStackTrace();
    }

    public void handleMessageEvent(DisposableMap dm, Thread thread, Event<FireStreamMessage> event) {
        if (event.isAdded()) {
            dm.add(addMessage(event.get(), thread));
        }
        if (event.isModified()) {
            // Just update the date for now
            Message message = ChatSDK.db().fetchEntityWithEntityID(event.get().getId(), Message.class);
            if (message != null) {
                FirestreamHelper.copyToMessage(message, event.get());
            }
        }
        if (event.isRemoved()) {
            if(!Fire.stream().getConfig().deleteMessagesOnReceipt) {
                removeMessage(event.get().getId(), thread.getEntityID());
            }
        }

    }
}
