package app.xmpp.adapter;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.listeners.XMPPMUCRoleListener;
import app.xmpp.adapter.listeners.XMPPSubjectUpdatedListener;
import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.adapter.utils.JidEntityID;
import app.xmpp.adapter.utils.PresenceHelper;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;


/**
 * Created by ben on 8/8/17.
 * https://github.com/igniterealtime/Smack/blob/master/documentation/extensions/muc.md
 */

public class XMPPMUCManager implements IncomingChatMessageListener {

    private WeakReference<XMPPManager> manager;
    private MultiUserChatManager chatManager;

    protected Map<String, XMPPMUCRoleListener> userStatusListeners = new HashMap<>();

    private DisposableMap dm = new DisposableMap();

    public XMPPMUCManager(XMPPManager manager) {
        this.manager = new WeakReference<>(manager);

        manager.chatManager().addIncomingListener(this);

        chatManager = MultiUserChatManager.getInstanceFor(manager.getConnection());
        chatManager.setAutoJoinOnReconnect(true);

        // Can use this maybe?
//        GroupChatInvitation invite = (GroupChatInvitation)result.getExtension("x","jabber:x:conference");

        chatManager.setAutoJoinFailedCallback((muc, e) -> {
            ChatSDK.events().onError(e);
            joinRoom(muc, true).subscribe();
        });

        chatManager.addInvitationListener((conn, room, inviter, reason, password, message, invitation) -> {
            Logger.debug("Before");
            joinRoom(room, true).subscribe();
        });
    }

    public Single<Thread> createRoom(final String name, final String description, final ArrayList<User> users, boolean isPublic) {
        return Single.defer(() -> {
            // Create a new group chat
            final String roomID = generateRoomId(name);

            final MultiUserChat chat = chatManager.getMultiUserChat(JidCreate.entityBareFrom(roomID));

            Jid currentJid = JidCreate.bareFrom(ChatSDK.currentUserID());

            chat.create(nickname());
//                    .getConfigFormManager()
//                    .setMembersOnly(!isPublic)
//                    .setRoomOwners(Collections.singleton(currentJid));

            FillableForm form = chat.getConfigurationForm().getFillableForm();

            form.setAnswer("muc#roomconfig_persistentroom", true);
            form.setAnswer("muc#roomconfig_publicroom", isPublic);
            form.setAnswer("muc#roomconfig_maxusers", 200);
            form.setAnswer("muc#roomconfig_whois", "anyone");
            form.setAnswer("muc#roomconfig_membersonly", !isPublic);
            form.setAnswer("muc#roomconfig_moderatedroom", true);

            form.setAnswer("muc#roomconfig_roomname", name);
            form.setAnswer("muc#roomconfig_roomdesc", description);
            form.setAnswer("muc#roomconfig_allowinvites", isPublic);

//            form.setAnswer("muc#roomconfig_roomowners", JidUtil.toStringList(Collections.singleton(currentJid)));

            chat.sendConfigurationForm(form);

            // If this fails, we will jit the error block immediately...
            chat.sendConfigurationForm(form);

            // Add the users
            List<Completable> completables = new ArrayList<>();
            for(User user: users) {
                completables.add(inviteUser(user, chat));
            }

            return Completable.merge(completables).andThen(joinRoom(roomID));

        }).subscribeOn(RX.io());
    }

    public Single<Thread> joinRoom(String roomJID) throws Exception {
        return joinRoom(getChat(roomJID), true);
    }

    public Single<Thread> joinRoom(MultiUserChat chat, boolean bookmark) {
        return Single.defer(() -> {

            Thread thread = threadForRoomID(chat.getRoom().toString());
            thread.setDeleted(false);

            Resourcepart nickname = nickname();
            if (nickname != null) {

                MucEnterConfiguration.Builder config = chat.getEnterConfigurationBuilder(nickname);

                // Work out how much history to request
                // TODO: For iOS we use the last online date - which is better?

                Date lastMessageDate = XMPPManager.shared().connectionManager().getLastOnline(ChatSDK.currentUserID(), thread.getEntityID());
//                if (lastMessageDate == null) {
//                    lastMessageDate = thread.lastMessageAddedDate();
//                    lastMessageDate = manager.get().clientToServerTime(lastMessageDate);
//                }

                if (lastMessageDate != null) {
                    lastMessageDate = manager.get().clientToServerTime(lastMessageDate);
                    config.requestHistorySince(lastMessageDate);
                }
                else {
                    config.requestMaxStanzasHistory(XMPPModule.config().mucMessageHistoryDownloadLimit);
                }

                // Add the message listener
                chat.removeMessageListener(manager.get().messageListener);
                chat.addMessageListener(manager.get().messageListener);

                XMPPSubjectUpdatedListener subjectUpdatedListener = new XMPPSubjectUpdatedListener(chat);
                dm.add(subjectUpdatedListener);

                XMPPMUCRoleListener userStatusListener = getUserStatusListener(thread.getEntityID());
                if (userStatusListener == null) {
                    userStatusListener = new XMPPMUCRoleListener(this, chat, thread);
                    putUserStatusListener(thread.getEntityID(), userStatusListener);
                }

                chat.join(config.build());

                RoomInfo info = chatManager.getRoomInfo(chat.getRoom());
                String name = info.getName();

                thread.setName(name, true);

                if(info.isMembersOnly()) {
                    thread.setType(ThreadType.PrivateGroup);
                } else {
                    thread.setType(ThreadType.PublicGroup);
                }

                if (bookmark) {
                    manager.get().bookmarkManager().addBookmarkedConference(name, chat.getRoom(), false, chat.getNickname(), null);
                }

                User user = ChatSDK.currentUser();

                // Send presence
                Presence presence = PresenceHelper.presenceForUser(user);
                chat.changeAvailabilityStatus(presence.getStatus(), presence.getMode());

                if (thread.addUser(user, false)) {
                    ChatSDK.events().source().accept(NetworkEvent.threadUserAdded(thread, user));
                    ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
                }

                // Get the member list
                return userStatusListener.updateAffiliates()
                        .flatMap(affiliates -> getRoomName(chat)).map(s -> {
                            thread.setName(s);
                            return thread;
                        });

//                return userStatusListener.updateAffiliates().flatMap(affiliates -> {
//
//                    List<Completable> completables = new ArrayList<>();
//
//                    // Make sure we are subscribed to the members
//                    for (Affiliate affiliate: affiliates) {
//                        completables.add(ChatSDK.core().getUserForEntityID(affiliate.getJid().asBareJid().toString()).doOnSuccess(user -> {
//                            if (affiliate.getAffiliation() == MUCAffiliation.owner) {
//                                thread.setCreator(user);
//                            }
//                            thread.addUser(user);
//                        }).ignoreElement());
//                    }
//
//                    return Completable.merge(completables).toSingle(() -> thread);
//                });
            }
            return Single.error(ChatSDK.getException(R.string.could_not_join_muc));

        }).doOnSuccess(thread -> {
            ChatSDK.events().source().accept(NetworkEvent.threadAdded(thread));
        }).subscribeOn(RX.io());
    }

    public Single<String> getRoomName(MultiUserChat chat) {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            RoomInfo info = chatManager.getRoomInfo(chat.getRoom());
            String name = info.getName();
            emitter.onSuccess(name);

        }).subscribeOn(RX.io());
    }

    public Resourcepart nickname() {
        return nickname(ChatSDK.currentUser());
    }

    public Resourcepart nickname(User user) {
        try {
            Jid jid = JidCreate.entityBareFrom(user.getEntityID());
            Localpart local = jid.getLocalpartOrNull();
            if (local != null) {
                return Resourcepart.from(local.toString());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public void joinChatFromBookmark(BookmarkedConference bookmark) {
        MultiUserChat chat = getChatOrNull(bookmark.getJid());
        if (chat != null) {
            joinRoom(chat, false).ignoreElement().subscribe(ChatSDK.events());
        } else {
            try {
                manager.get().bookmarkManager().removeBookmarkedConference(bookmark.getJid());
                Thread thread = threadForRoomID(bookmark.getJid().toString());
                ChatSDK.thread().sendLocalSystemMessage(ChatSDK.getString(R.string.room_no_longer_active), thread);
                // TODO: Handle this
            } catch (Exception e) {}
        }
    }

    public Completable destroy(Thread thread) {
        return Completable.defer(() -> {
            MultiUserChat chat = getChatOrNull(thread.getEntityID());
            if (chat != null) {
                chat.destroy(null, null);
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    public Completable updateName(Thread thread, String name) {
        return Completable.defer(() -> {
            MultiUserChat chat = getChatOrNull(thread.getEntityID());
            if (chat != null) {
                chat.changeSubject(name);
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    public MultiUserChat getChatOrNull(EntityBareJid jid) {
        try {
            // This will throw an exception if the room doesn't exist
            chatManager.getRoomInfo(jid);
            return chatManager.getMultiUserChat(jid);
        } catch (Exception e) {
            return null;
        }
    }

    public MultiUserChat getChat(String jid) throws Exception {
        return chatManager.getMultiUserChat(JidCreate.entityBareFrom(jid));
    }

    public MultiUserChat getChatOrNull(String jid) {
        try {
            return getChatOrNull(JidCreate.entityBareFrom(jid));
        } catch (Exception e) {
            return null;
        }
    }

    public Completable inviteUser(final User user, MultiUserChat chat) {
        return Completable.create(e -> {
            Jid jid = JidEntityID.fromEntityID(user.getEntityID());
            chat.invite(JidCreate.entityBareFrom(user.getEntityID()), "");
            chat.grantMembership(jid);

            // TODO: Workaround for Android, send the invitation by a message too
            // On Android, if the user is offline, the invitation is never sent
//            Message message = XMPPMessageBuilder.create()
//                    .setTo(jid.asBareJid())
//                    .addGroupInviteExtension(chat.getRoom().asBareJid())
//                    .build();
//
//            manager.get().sendStanza(message);

            e.onComplete();
        }).subscribeOn(RX.io());
    }

    public Completable inviteUser(final Thread thread, final User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            if (chat != null) {
                return inviteUser(user, chat);
            }
            return Completable.error(ChatSDK.getException(R.string.permission_denied));
        }).subscribeOn(RX.io());
    }

    public Completable removeUser(final User user, MultiUserChat chat) {
        return Completable.create(e -> {
            Jid jid = JidEntityID.fromEntityID(user.getEntityID());
            chat.revokeMembership(jid);
            chat.kickParticipant(nickname(user), "");
            e.onComplete();
        }).subscribeOn(RX.io());
    }

    public Completable removeUser(final Thread thread, final User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            if (chat != null) {
                return removeUser(user, chat);
            }
            return Completable.error(ChatSDK.getException(R.string.permission_denied));
        }).subscribeOn(RX.io());
    }

    // Make a room ID in the form - name_[current date in hex]
    private String generateRoomId(String name) throws Exception {
        String domain = mucServiceDomain();

        long roomIntID = new Date().getTime() / 1000;

        return name.replaceAll("[^a-zA-Z0-9]", "") + "_" + String.format("%08X", roomIntID) + "@" + domain;
    }

    public String mucServiceDomain() throws Exception {
        List<DomainBareJid> services = chatManager.getMucServiceDomains();
        if(services.size() > 0) {
            return services.get(0).toString();
        } else {
            throw ChatSDK.getException(R.string.no_muc_service_available);
        }
    }

    public Thread threadForRoomID(MultiUserChat chat) {
        return threadForRoomID(chat.getRoom().toString());
    }

    public Thread threadForRoomID(String roomJID) {
        Logger.debug("Thread For Room " + roomJID);
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(roomJID);
        if(thread == null) {
            thread = ChatSDK.db().createEntity(Thread.class);

            thread.setEntityID(roomJID);

            thread.setCreator(ChatSDK.currentUser());
            thread.setCreationDate(new Date());
            thread.setType(ThreadType.PrivateGroup);
            ChatSDK.db().update(thread);
            ChatSDK.events().source().accept(NetworkEvent.threadAdded(thread));
        }
        return thread;
    }

    public MultiUserChat chatForThreadID (String threadID) {
        try {
            return chatManager.getMultiUserChat(JidCreate.entityBareFrom(threadID));
        }
        catch (XmppStringprepException e) {
            ChatSDK.events().onError(e);
        }
        return null;
    }

    public String userJID(MultiUserChat room, Jid userRoomID) {
        try {
            return room.getOccupant(JidCreate.entityFullFrom(userRoomID.toString())).getJid().asBareJid().toString();
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
        return null;
    }

    /**
     *
     * @param from
     * @return
     */
    public String userJID(Jid from) {
        MultiUserChat chat = chatManager.getMultiUserChat(from.asEntityBareJidIfPossible());
        if (chat != null) {
            Occupant occupant = chat.getOccupant(from.asEntityFullJidIfPossible());
            if (occupant != null) {
                return occupant.getJid().asBareJid().toString();
            } else {
                Logger.debug("Test");
            }
        }


        return null;
    }

    public void dispose () {
        dm.dispose();
    }

    public Single<List<Affiliate>> requestAffiliatesFromServer(Thread thread) {
        return Single.create((SingleOnSubscribe<List<Affiliate>>)emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            emitter.onSuccess(getAffiliates(chat));
        }).subscribeOn(RX.io());
    }

    public List<Affiliate> getAffiliates(MultiUserChat chat) {
        List<Affiliate> affiliates = new ArrayList<>();
        if (chat != null) {
            try {
                affiliates.addAll(chat.getOwners());
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            }
            try {
                affiliates.addAll(chat.getAdmins());
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            }
            try {
                affiliates.addAll(chat.getMembers());
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            }
            try {
                affiliates.addAll(chat.getOutcasts());
            } catch (Exception e) {
                // This will be triggered if we are not admin or owner so ignore it
//                ChatSDK.events().onError(e);
            }
        }
        return affiliates;
    }

//    public int getRoleForUser(Thread thread, User user) {
//        return Role.fromAffiliate(getAffiliateForUser(thread, user));
//    }

    public MUCAffiliation getAffiliation(Thread thread, User user) {
        XMPPMUCRoleListener listener = getUserStatusListener(thread.getEntityID());
        if (listener != null) {
            MUCAffiliation affiliation = listener.getAffiliation(user);
            return affiliation != null ? affiliation : MUCAffiliation.none;
        }
        return MUCAffiliation.none;
    }

    public MUCRole getRole(Thread thread, User user) {
        XMPPMUCRoleListener listener = getUserStatusListener(thread.getEntityID());
        if (listener != null) {
            MUCRole role = listener.getRole(user);
            return role != null ? role : MUCRole.none;
        }
        return MUCRole.none;
    }

    public Resourcepart getNick(Thread thread, User user) {
        XMPPMUCRoleListener listener = getUserStatusListener(thread.getEntityID());
        if (listener != null) {
            return listener.getNick(user);
        }
        return null;
    }

    public Completable grantModerator(Thread thread, User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            MUCRole role = getRole(thread, user);
            Resourcepart nick = getNick(thread, user);
            if (chat != null && nick != null) {
                // TODO: HERE we need to have the nickname so we need the affiliate
                if (role != MUCRole.moderator) {
                    chat.grantModerator(nick);
                    return Completable.complete();
//                    return refreshRoomAffiliation(chat);
                } else {
                    return Completable.complete();
                }
            } else {
                return Completable.error(ChatSDK.getException(R.string.permission_denied));
            }
        }).subscribeOn(RX.io());
    }

    public Completable revokeModerator(Thread thread, User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            MUCRole role = getRole(thread, user);
            Resourcepart nick = getNick(thread, user);
            if (chat != null && nick != null) {
                if (role == MUCRole.moderator) {
                    chat.revokeModerator(nick);
                    return Completable.complete();
//                    return refreshRoomAffiliation(chat);
                } else {
                    return Completable.complete();
                }
            } else {
                return Completable.error(ChatSDK.getException(R.string.permission_denied));
            }
        }).subscribeOn(RX.io());
    }

    public Completable grantVoice(Thread thread, User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Resourcepart nick = getNick(thread, user);
            if (chat != null && nick != null) {
                chat.grantVoice(nick);
                return Completable.complete();
//                return refreshRoomAffiliation(chat);
            } else {
                return Completable.error(ChatSDK.getException(R.string.permission_denied));
            }
        }).subscribeOn(RX.io());
    }

    public Completable revokeVoice(Thread thread, User user) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Resourcepart nick = getNick(thread, user);
            if (chat != null && nick != null) {
                chat.revokeVoice(nick);
                return Completable.complete();
//                return refreshRoomAffiliation(chat);
            } else {
                return Completable.error(ChatSDK.getException(R.string.permission_denied));
            }
        }).subscribeOn(RX.io());
    }

    public Completable setRole(Thread thread, User user, MUCAffiliation affiliation) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            MUCAffiliation currentAffiliation = getAffiliation(thread, user);
            Jid jid = JidCreate.bareFrom(user.getEntityID());

            if (chat != null && currentAffiliation != affiliation) {
                if (affiliation == MUCAffiliation.owner) {
                    chat.grantOwnership(jid);
                }
                else if (affiliation == MUCAffiliation.admin) {
                    chat.grantAdmin(jid);
                }
                else if (affiliation == MUCAffiliation.member) {
                    chat.grantMembership(jid);
                }
                else if (affiliation == MUCAffiliation.outcast) {
//                    chat.grantMembership(affiliate.getJid());
                    chat.banUser(jid, " ");
                }
                else {
                    chat.revokeMembership(jid);
                }
            }
            return Completable.complete();
//            return refreshRoomAffiliation(chat);
        }).subscribeOn(RX.io());
    }

    public Completable refreshRoomAffiliation(Thread thread) {
        return Completable.defer(() -> {
            MultiUserChat chat = getChat(thread.getEntityID());
            return refreshRoomAffiliation(chat);
        }).subscribeOn(RX.io());
    }

    protected Completable refreshRoomAffiliation(MultiUserChat chat) {
        return Completable.defer(() -> {
            XMPPMUCRoleListener listener = getUserStatusListener(chat.getRoom().toString());
            if (listener != null) {
                return listener.updateAffiliates().ignoreElement();
            }
            return Completable.complete();
        }).subscribeOn(RX.io());
    }

    public XMPPMUCRoleListener getUserStatusListener(String threadEntityID) {
        return userStatusListeners.get(threadEntityID);
    }

    public void putUserStatusListener(String threadEntityID, XMPPMUCRoleListener listener) {
        userStatusListeners.put(threadEntityID, listener);
    }

    public void deactivateThread(Thread thread) {
        for (UserThreadLink l: thread.getUserThreadLinks()) {
            // If they are banned, they are hidden anyway
            if (!l.isBanned()) {
                boolean updated = l.setIsActive(false);
//                updated = l.setRole(MUCRole.none.name()) || updated;
//                updated = l.setAffiliation(MUCAffiliation.none.name()) || updated;
                if (updated) {
                    ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, l.getUser()));
                }
            }
        }
    }

    @Override
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        XMPPMessageWrapper xmr = new XMPPMessageWrapper(message);

        Logger.debug("Ok");
        if (xmr.isSilent()) {
            // TODO: Handle invite for MUC if we were not online when the invitation was received
            if (xmr.hasAction(MessageType.Action.GroupInvite)) {
                if (xmr.getMessage().hasExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE)) {
                    // This was received when we were offline
                    // If we are online, we should have received the actual MUC invite
                    StandardExtensionElement element = xmr.extras().getFirstElement(XMPPDefines.ID);
                    if (element != null) {
                        String chatId = element.getText();
                        if (chatId != null && !chatId.isEmpty()) {
                            Thread theThread = ChatSDK.db().fetchThreadWithEntityID(chatId);
                            // Check to see if this room already exists?
                            if (theThread == null) {
                                // Join the room
                                try {
                                    joinRoom(chatId).subscribe();
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
            }
        }
    }
}
