package app.xmpp.adapter.handlers;

import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.XMPPMessageBuilder;
import app.xmpp.adapter.utils.Role;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.base.AbstractThreadHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.push.AbstractPushHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.types.ReadStatus;
import sdk.guru.common.RX;


/**
 * Created by benjaminsmiley-andrews on 01/07/2017.
 */

public class XMPPThreadHandler extends AbstractThreadHandler {

    @Override
    public Single<Thread> createThread(String name, List<User> theUsers, int type, String entityID, String imageURL, Map<String, Object> meta) {
        return Single.defer((Callable<SingleSource<Thread>>) () -> {

            if (entityID != null) {
                Thread t = ChatSDK.db().fetchThreadWithEntityID(entityID);
                if (t != null) {
                    return Single.just(t);
                }
            }

            ArrayList<User> users = new ArrayList<>(theUsers);

            User currentUser = ChatSDK.currentUser();

            // Make sure that the current user is in the list and
            // that they are not the first item
            users.remove(currentUser);
            users.add(currentUser);

            if (users.size() == 2 && (type == ThreadType.None || ThreadType.is(type, ThreadType.Private1to1))) {
                User otherUser = users.get(0);

                // Check if the thread exists
                Thread thread = ChatSDK.db().fetchThreadWithEntityID(otherUser.getEntityID());
                if (thread != null && (ChatSDK.config().reuseDeleted1to1Threads || !thread.isDeleted())) {
                    thread.setDeleted(false);
                    return Single.just(thread);
                }

                thread = ChatSDK.db().createEntity(Thread.class);
                thread.setCreator(currentUser);
                thread.setCreationDate(new Date());
                thread.setName(name);
                thread.setImageUrl(imageURL);
                thread.setEntityID(users.get(0).getEntityID());
                thread.setType(ThreadType.Private1to1);
                thread.addUsers(users);
                ChatSDK.db().update(thread);


                return Single.just(thread);
            } else if (users.size() > 2 && (type == ThreadType.None || ThreadType.isGroup(type))) {
                users.remove(currentUser);
                return XMPPManager.shared().mucManager.createRoom(name, "", users, ThreadType.isPublic(type));
            } else {
                return Single.error(ChatSDK.getException(R.string.unable_to_create_thread));
            }

        }).subscribeOn(RX.io());

    }

    @Override
    public boolean canRemoveUserFromThread(Thread thread, User user) {
        if (thread.typeIs(ThreadType.Group)) {
            String myRole = roleForUser(thread, ChatSDK.currentUser());
            String role = roleForUser(thread, user);
            return Role.isOwnerOrAdmin(myRole) && Role.isMember(role);
        }
        return false;
    }

    @Override
    public Completable removeUsersFromThread(Thread thread, List<User> users) {
        return Completable.defer(() -> {
            List<Completable> completables = new ArrayList<>();
            for (User user: users) {
                completables.add(XMPPManager.shared().mucManager.setRole(thread, user, MUCAffiliation.outcast));
            }
            return Completable.concat(completables);
        });
    }

    @Override
    public boolean canAddUsersToThread(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            String myRole = roleForUser(thread, ChatSDK.currentUser());
            return Role.isOwnerOrAdmin(myRole);
        }
        return false;

    }

    @Override
    public Completable addUsersToThread(final Thread thread, List<User> users) {
        return Completable.defer(() -> {
            List<Completable> completables = new ArrayList<>();
            for (User user: users) {
                completables.add(XMPPManager.shared().mucManager.inviteUser(thread, user));
            }
            return Completable.concat(completables);
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable deleteThread(Thread thread) {
        return leaveThread(thread).doOnComplete(() -> {

        }).doFinally(() -> {
            ChatSDK.events().source().accept(NetworkEvent.threadRemoved(thread));
            thread.removeMessagesAndMarkDeleted();
        });

//        return Completable.defer(() -> {
//
//            thread.cascadeDelete();
////            List<Message> messages = thread.getMessages();
////            for (Message m : messages) {
////                m.cascadeDelete();
////            }
////            if (thread.typeIs(ThreadType.Group)) {
////                return leaveThread(thread).andThen(Completable.create(emitter -> {
////                    DaoCore.deleteEntity(thread);
////                    emitter.onComplete();
////                }));
////            } else {
////                thread.setDeleted(true);
////                return Completable.complete();
////            }
//            return Completable.complete();
//        }).subscribeOn(RX.io());
    }

    @Override
    public Completable leaveThread(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(thread.getEntityID());

                if (chat != null) {
                    XMPPManager.shared().bookmarkManager().removeBookmarkedConference(chat.getRoom());

                    // Mark the room as having left
                    chat.sendMessage(XMPPMessageBuilder.create().addLeaveGroupExtension().build());
                    try {
                        chat.leave();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    User user = ChatSDK.currentUser();
                    UserThreadLink link = thread.getUserThreadLink(user.getId());

                    XMPPManager.shared().mucManager.deactivateThread(thread);

                    if(link == null || link.setHasLeft(true)) {
                        ChatSDK.events().source().accept(NetworkEvent.threadUserRemoved(thread, user));
                        ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
                    }
                }
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable joinThread(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                return XMPPManager.shared().mucManager.joinRoom(thread.getEntityID()).ignoreElement();
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    @Override
    public boolean canJoinThread(Thread thread) {
        if (thread.typeIs(ThreadType.Group) && ChatSDK.config().allowUserToRejoinGroup) {
            // Get the link
            UserThreadLink link = thread.getUserThreadLink(ChatSDK.currentUser().getId());
            if (link != null && link.hasLeft() && !link.isBanned()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canLeaveThread(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            // Get the link
            UserThreadLink link = thread.getUserThreadLink(ChatSDK.currentUser().getId());
            if (link != null && !link.hasLeft() && !link.isBanned()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Completable deleteMessage(Message message) {
        return Completable.create(emitter -> {
            message.getThread().removeMessage(message);
        });
//
//        return Completable.error(new Throwable("Message deletion is not supported"));
    }

    @Override
    public boolean canDeleteMessage(Message message) {
        return true;
    }

    @Override
    public Completable sendMessage(final Message message) {
        return Completable.create(emitter -> {

            if (ChatSDK.readReceipts() != null) {
                message.setupInitialReadReceipts();
            }

            XMPPMessageBuilder builder = new XMPPMessageBuilder()
                    .setType(message.getType())
                    .setEntityID(message.getEntityID());

            Map<String, String> meta = null;
            if (ChatSDK.encryption() != null) {
                meta = ChatSDK.encryption().encrypt(message);
            }
            if (meta == null) {
                meta = message.getMetaValuesAsMap();
            }
            builder.setValues(meta);

            if (meta.containsKey(Keys.MessageEncryptedPayloadKey)) {
                String body = meta.get(Keys.MessageText);
                if (body != null) {
                    builder.setBody(body);
                }
            } else {
                if(message.getMessageType().is(MessageType.Location)) {
                    builder.setLocation(message.getLocation());
                }
                else if(message.getMessageType().is(MessageType.Image)) {
                    builder.setBody((String) message.valueForKey(Keys.MessageImageURL));
                } else {
                    builder.setBody(message.getText());
                }
            }

            Exception sendError = null;

            if(message.getThread().typeIs(ThreadType.Private1to1)) {

                builder.setAsChatType();
                builder.setTo(JidCreate.entityBareFrom(message.getThread().getEntityID()));

                sendError = XMPPManager.shared().sendStanza(builder.build());
            }
            else if (message.getThread().typeIs(ThreadType.Group)) {
                builder.setAsGroupChatType();
                builder.setTo(JidCreate.entityBareFrom(message.getThread().getEntityID()));

                if (ChatSDK.readReceipts() != null) {
                    builder.addDeliveryReceiptRequest();
                }

                sendError = XMPPManager.shared().sendStanza(builder.build());
            } else {
                emitter.onComplete();
            }

            if (sendError != null) {
                emitter.onError(sendError);
            } else {
                if (ChatSDK.push() != null && ChatSDK.config().clientPushEnabled && message.getThread().typeIs(ThreadType.Private)) {
                    Map<String, Object> data = ChatSDK.push().pushDataForMessage(message);
                    if (data != null) {
                        // Fix a bug with the default implementation
                        // In XMPP 1-to-1 threads have the ID of the other user. So we need to put our
                        // ID as the thread ID
                        Object senderId = data.get(AbstractPushHandler.SenderId);
                        if (message.getThread().typeIs(ThreadType.Private1to1) && senderId != null) {
                            data.put(AbstractPushHandler.ThreadId, senderId);
                        }
                        ChatSDK.push().sendPushNotification(data);
                    }
                }
                emitter.onComplete();
            }

        }).doOnComplete(() -> message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date())).subscribeOn(RX.io());
    }

    @Override
    public Completable pushThread(Thread thread) {
        return Completable.complete();
    }

//    @Override
//    public Completable pushThreadMeta(Thread thread) {
//        return XMPPManager.shared().mucManager.updateName(thread, thread.getName());
//    }

    @Override
    public boolean muteEnabled(Thread thread) {
        return false;
    }


    @Override
    public boolean canDestroy(Thread thread) {
        if(ChatSDK.config().threadDestructionEnabled && thread.typeIs(ThreadType.Group) && rolesEnabled(thread)) {
            String myRole = roleForUser(thread, ChatSDK.currentUser());
            return Role.isOwner(myRole);
        }
        return false;
    }

    @Override
    public Completable destroy(Thread thread) {
        return XMPPManager.shared().mucManager.destroy(thread);
    }

    // Moderation
    @Override
    public Completable grantVoice(Thread thread, User user) {
        if (!hasVoice(thread, user)) {
            return XMPPManager.shared().mucManager.grantVoice(thread, user).concatWith(super.grantVoice(thread, user)).subscribeOn(RX.io());
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable revokeVoice(Thread thread, User user) {
        if (hasVoice(thread, user)) {
            return XMPPManager.shared().mucManager.revokeVoice(thread, user).concatWith(super.revokeVoice(thread, user)).subscribeOn(RX.io());
        } else {
            return Completable.complete();
        }
    }

    @Override
    public boolean hasVoice(Thread thread, User user) {

        if (thread.typeIs(ThreadType.Group)) {
            UserThreadLink link = thread.getUserThreadLink(user.getId());
            if (link != null && link.hasLeft()) {
                return false;
            }
            String role = XMPPManager.shared().mucManager.getRole(thread, user).name();

            if (Role.isNone(role)) {
                String affiliation = XMPPManager.shared().mucManager.getAffiliation(thread, user).name();
                return !Role.isOutcastOrNone(affiliation);
            } else {
                return Role.isModeratorOrParticipant(role);
            }
        }
        return true;
    }

    public boolean canChangeVoice(Thread thread, User user) {
        // Are they active?
        UserThreadLink link = thread.getUserThreadLink(user.getId());
        if (link != null && link.isActive()) {
            boolean isModerator = isModerator(thread, user);
            boolean amModerator = isModerator(thread, ChatSDK.currentUser());
            return amModerator && !isModerator;
        }
        return false;
    }

    @Override
    public Completable grantModerator(Thread thread, User user) {
        if (!isModerator(thread, user)) {
            return XMPPManager.shared().mucManager.grantModerator(thread, user).subscribeOn(RX.io());
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable revokeModerator(Thread thread, User user) {
        if (isModerator(thread, user)) {
            return XMPPManager.shared().mucManager.revokeModerator(thread, user).subscribeOn(RX.io());
        } else {
            return Completable.complete();
        }
    }

    @Override
    public boolean canChangeModerator(Thread thread, User user) {
        UserThreadLink link = thread.getUserThreadLink(user.getId());
        if (link != null && link.isActive()) {
            String myRole = roleForUser(thread, ChatSDK.currentUser());
            String role = roleForUser(thread, user);
            return Role.isOwnerOrAdmin(myRole) && !Role.isOwnerOrAdmin(role);
        }
        return false;
    }

    @Override
    public boolean isModerator(Thread thread, User user) {
        UserThreadLink link = thread.getUserThreadLink(user.getId());
        if (link != null && link.hasLeft()) {
            return false;
        }
        String role = XMPPManager.shared().mucManager.getRole(thread, user).name();
        if (Role.isNone(role)) {
            String affiliation = XMPPManager.shared().mucManager.getAffiliation(thread, user).name();
            return Role.isOwnerOrAdmin(affiliation);
        }
        return Role.isModerator(role);
    }

    public List<String> availableRoles(Thread thread, User user) {
        List<String> roles = new ArrayList<>();

        String myRole = roleForUser(thread, ChatSDK.currentUser());

        if (Role.isOwner(myRole)) {
            roles.add(MUCAffiliation.owner.name());
            roles.add(MUCAffiliation.admin.name());
            roles.add(MUCAffiliation.member.name());
        }
        return roles;

    }

    @Override
    public String localizeRole(String role) {
        return Role.toString(role);
    }

    @Override
    public boolean rolesEnabled(Thread thread) {
        return ChatSDK.config().rolesEnabled && thread.typeIs(ThreadType.Group);
    }

    @Override
    public boolean canChangeRole(Thread thread, User user) {
        return rolesEnabled(thread) && availableRoles(thread, user).size() > 1;
    }

    public String roleForUser(Thread thread, User user) {
        UserThreadLink link = thread.getUserThreadLink(user.getId());
        if (link != null && !link.hasLeft()) {
            return link.getAffiliation();
        } else {
            return MUCAffiliation.none.name();
        }
    }

    public Completable setRole(String role, Thread thread, User user) {
        return Completable.defer(() -> {
            MUCAffiliation newAffiliation = MUCAffiliation.fromString(role);
            if (newAffiliation != null) {
                return XMPPManager.shared().mucManager.setRole(thread, user, newAffiliation);
            }
            return Completable.error(ChatSDK.getException(R.string.permission_denied));
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable refreshRoles(Thread thread) {
        return XMPPManager.shared().mucManager.refreshRoomAffiliation(thread);
    }

    @Override
    public boolean canRefreshRoles(Thread thread) {
        String role = roleForUser(thread, ChatSDK.currentUser());
        return rolesEnabled(thread) && Role.canRead(role);
    }

    @Override
    public boolean canEditThreadDetails(Thread thread) {
//        if (thread.typeIs(ThreadType.Group)) {
//            String role = roleForUser(thread, ChatSDK.currentUser());
//            return Role.isOwnerOrAdmin(role);
//        }
        return false;
    }

    @Override
    public boolean isActive(Thread thread, User user) {
        if (thread != null && user != null && thread.getUserThreadLink(user.getId()) != null) {
            return thread.getUserThreadLink(user.getId()).isActive();
        }
        return false;
    }

    @Override
    public String generateNewMessageID(Thread thread) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String readableEntityId(String entityID) {
        String readable = super.readableEntityId(entityID);
        try {
            Jid jid = JidCreate.bareFrom(entityID);
            if (jid.hasLocalpart()) {
                readable = jid.getLocalpartOrNull().toString();
            }
        } catch (Exception e) {

        }
        return readable;
    }

}
