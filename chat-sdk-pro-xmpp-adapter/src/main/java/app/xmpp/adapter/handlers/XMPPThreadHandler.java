package app.xmpp.adapter.handlers;

import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPMUCManager;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.XMPPMessageBuilder;
import app.xmpp.adapter.utils.Role;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.base.AbstractThreadHandler;
import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.push.AbstractPushHandler;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageSendStatus;
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
                thread.update();
                return Single.just(thread);
            } else if (users.size() > 2 && (type == ThreadType.None || type == ThreadType.PrivateGroup)) {
                users.remove(currentUser);
                return XMPPManager.shared().mucManager.createRoom(name, "", users);
            } else {
                return Single.error(ChatSDK.getException(R.string.unable_to_create_thread));
            }

        }).subscribeOn(RX.io());

    }

    @Override
    public boolean canRemoveUsersFromThread(Thread thread, List<User> users) {
        // TODO: this isn't currently working properly
        return false;
//        if (thread.typeIs(ThreadType.Group)) {
//            int role = XMPPManager.shared().mucManager.getRoleForUser(thread, ChatSDK.currentUser());
//            return Role.isOr(role, Role.Owner, Role.Admin, Role.Moderator);
//        }
//        return false;
    }

    @Override
    public Completable removeUsersFromThread(Thread thread, List<User> users) {
        return Completable.defer(() -> {
            List<Completable> completables = new ArrayList<>();
            for (User user: users) {
                completables.add(XMPPManager.shared().mucManager.removeUser(thread, user));
            }
            return Completable.concat(completables);
        });
    }

    @Override
    public boolean canAddUsersToThread(Thread thread) {
        if (thread.typeIs(ThreadType.Group)) {
            int role = XMPPManager.shared().mucManager.getRoleForUser(thread, ChatSDK.currentUser());
            return !Role.isOr(role, Role.None, Role.Outcast, Role.Visitor);
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
        return Completable.defer(() -> {
            List<Message> messages = thread.getMessages();
            for (Message m : messages) {
                DaoCore.deleteEntity(m);
            }
            if (thread.typeIs(ThreadType.Group)) {
                return leaveThread(thread).andThen(Completable.create(emitter -> {
                    DaoCore.deleteEntity(thread);
                    emitter.onComplete();
                }));
            } else {
                thread.setDeleted(true);
                return Completable.complete();
            }
        }).subscribeOn(RX.io());
    }

    @Override
    public Completable leaveThread(Thread thread) {
        return Completable.defer(() -> {
            if (thread.typeIs(ThreadType.Group)) {
                MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(thread.getEntityID());
                if (chat != null) {
                    chat.leave();
                    XMPPManager.shared().bookmarkManager().removeBookmarkedConference(chat.getRoom());
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
    public Completable deleteMessage(Message message) {
        return Completable.error(new Throwable("Message deletion is not supported"));
    }

    @Override
    public boolean canDeleteMessage(Message message) {
        return false;
    }

    @Override
    public Completable sendMessage (final Message message) {
        return Completable.create(emitter -> {
            XMPPMessageBuilder builder = new XMPPMessageBuilder()
                    .setType(message.getType())
                    .setValues(message.getMetaValuesAsMap())
                    .setEntityID(message.getEntityID())
                    .setBody(message.getText());

            if(message.getMessageType().is(MessageType.Location)) {
                builder.setLocation(message.getLocation());
            }
            if(message.getMessageType().is(MessageType.Image)) {
                builder.setBody((String) message.valueForKey(Keys.MessageImageURL));
            }


            if(message.getThread().typeIs(ThreadType.Private1to1)) {
//                ChatManager chatManager = XMPPManager.shared().chatManager();
//                Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(message.getThread().getEntityID()));

                // Unlock the resource using reflection
//                Class<?> chatClass = chat.getClass();
//                Method unlockMethod = chatClass.getDeclaredMethod("unlockResource");
//                if (unlockMethod != null) {
//                    unlockMethod.setAccessible(true);
//                    unlockMethod.invoke(chat);
//                }

                builder.setAsChatType();
                builder.setTo(JidCreate.entityBareFrom(message.getThread().getEntityID()));


                XMPPManager.shared().sendStanza(builder.build());
//                chat.send(builder.build());
            }
            else if (message.getThread().typeIs(ThreadType.Group)) {
//                MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(message.getThread().getEntityID());
//                if(chat != null) {
                    builder.setAsGroupChatType();
                    builder.setTo(JidCreate.entityBareFrom(message.getThread().getEntityID()));
                    XMPPManager.shared().sendStanza(builder.build());
//                    chat.sendMessage(builder.build());
//                }
//                else {
//                    emitter.onError(new Throwable("Unable send message to group chat"));
//                }
            }

            if (ChatSDK.push() != null && message.getThread().typeIs(ThreadType.Private)) {
                HashMap<String, Object> data = ChatSDK.push().pushDataForMessage(message);

                // Fix a bug with the default implementation
                // In XMPP 1-to-1 threads have the ID of the other user. So we need to put our
                // ID as the thread ID
                if (message.getThread().typeIs(ThreadType.Private1to1) && data != null) {
                    data.put(AbstractPushHandler.ThreadId, AbstractPushHandler.SenderId);
                }

                ChatSDK.push().sendPushNotification(data);
            }

            message.setMessageStatus(MessageSendStatus.Sent);

            emitter.onComplete();
        }).doOnComplete(() -> message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date())).subscribeOn(RX.io());
    }

    @Override
    public Completable pushThread(Thread thread) {
        return Completable.complete();
    }

    @Override
    public boolean muteEnabled(Thread thread) {
        return false;
    }

    // Moderation
    @Override
    public Completable grantVoice(Thread thread, User user) {
        return XMPPManager.shared().mucManager.grantVoice(thread, user).concatWith(super.grantVoice(thread, user)).subscribeOn(RX.io());
    }

    @Override
    public Completable revokeVoice(Thread thread, User user) {
        return XMPPManager.shared().mucManager.revokeVoice(thread, user).concatWith(super.revokeVoice(thread, user)).subscribeOn(RX.io());
    }

    @Override
    public boolean hasVoice(Thread thread, User user) {
        if (thread.typeIs(ThreadType.Group)) {
            int role = XMPPManager.shared().mucManager.getRoleForUser(thread, user);
            return !Role.isOr(role, Role.Outcast, Role.None, Role.Visitor);
        }
        return true;
    }

    public boolean canChangeVoice(Thread thread, User user) {
        int myRole = XMPPManager.shared().mucManager.getRoleForUser(thread, ChatSDK.currentUser());
        int theirRole = XMPPManager.shared().mucManager.getRoleForUser(thread, user);

        // We can remove the voice from normal members if we are a super user
        return Role.isOr(myRole, Role.Owner, Role.Admin, Role.Moderator) && Role.isOr(theirRole, Role.Visitor, Role.Member, Role.Participant);
    }


    @Override
    public Completable grantModerator(Thread thread, User user) {
        return XMPPManager.shared().mucManager.grantModerator(thread, user).subscribeOn(RX.io());
    }

    @Override
    public Completable revokeModerator(Thread thread, User user) {
        return XMPPManager.shared().mucManager.revokeModerator(thread, user).subscribeOn(RX.io());
    }

    @Override
    public boolean canChangeModerator(Thread thread, User user) {
        int myRole = XMPPManager.shared().mucManager.getRoleForUser(thread, ChatSDK.currentUser());
        int theirRole = XMPPManager.shared().mucManager.getRoleForUser(thread, user);

        // We can change the moderation status if we are an admin or an owner and they
        // are not a member
        return Role.isOr(myRole, Role.Owner, Role.Admin) && Role.isOr(theirRole, Role.Member, Role.Moderator) && !Role.isOr(theirRole, Role.Owner, Role.Admin);
    }

    @Override
    public boolean isModerator(Thread thread, User user) {
        int role = XMPPManager.shared().mucManager.getRoleForUser(thread, user);
        return Role.isOr(role, Role.Moderator, Role.Admin, Role.Owner);
    }

    protected interface Adder {
        void add(String item);
    }

    public List<String> availableRoles(Thread thread, User user) {
        List<String> roles = new ArrayList<>();

        Adder adder = item -> {
            if (!roles.contains(item)) {
                roles.add(item);
            }
        };

        int myRole = XMPPManager.shared().mucManager.getRoleForUser(thread, ChatSDK.currentUser());
        int theirRole = XMPPManager.shared().mucManager.getRoleForUser(thread, user);

        // If I am the owner and they are an admin, I can promote or demote them
        if ((Role.is(myRole, Role.Owner)) && Role.is(theirRole, Role.Admin)) {
            adder.add(Role.toString(MUCAffiliation.owner));
            adder.add(Role.toString(MUCAffiliation.admin));
            adder.add(Role.toString(MUCAffiliation.member));
        } else {
            // If I am the owner and they are a member, I can set them to any role
            if (Role.is(myRole, Role.Owner)) {
                adder.add(Role.toString(MUCAffiliation.owner));
            }
            // If I am an admin and they are not an admin or owner, then I can make them an outcast
            if ((Role.isOr(myRole, Role.Admin, Role.Owner)) && !Role.isOr(theirRole, Role.Admin, Role.Owner)) {
                adder.add(Role.toString(MUCAffiliation.admin));
                adder.add(Role.toString(MUCAffiliation.member));
                adder.add(Role.toString(MUCAffiliation.outcast));
            }
        }

//        int userRole = XMPPManager.shared().mucManager.getRoleForUser(thread, user);
//        String affiliation = Role.toString(Role.intToAffiliation(userRole));
//        roles.remove(affiliation);

        return roles;
    }

    @Override
    public boolean rolesEnabled(Thread thread) {
        return ChatSDK.config().rolesEnabled && thread.typeIs(ThreadType.Group);
    }

    @Override
    public boolean canChangeRole(Thread thread, User user) {
        return rolesEnabled(thread) && !availableRoles(thread, user).isEmpty();
    }

    public String roleForUser(Thread thread, User user) {
        XMPPMUCManager manager = XMPPManager.shared().mucManager;
        Affiliate affiliate = manager.getAffiliateForUser(thread, user);
        if (affiliate != null) {
            return Role.toString(affiliate.getAffiliation());
        } else {
            return null;
        }
    }

    public Completable setRole(String role, Thread thread, User user) {
        return Completable.defer(() -> {
            MUCAffiliation newAffiliation = Role.affiliationFromString(role);
            if (newAffiliation != null) {
                return XMPPManager.shared().mucManager.setRole(thread, user, newAffiliation);
            }
            return Completable.error(ChatSDK.getException(R.string.permission_denied));
        }).subscribeOn(RX.io());
    }

}
