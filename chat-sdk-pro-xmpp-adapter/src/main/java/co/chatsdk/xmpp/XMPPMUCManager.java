package co.chatsdk.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.CurrentLocale;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.xmpp.listeners.XMPPChatParticipantListener;
import co.chatsdk.xmpp.listeners.XMPPMUCMessageListener;
import co.chatsdk.xmpp.listeners.XMPPMUCUserStatusListener;
import co.chatsdk.xmpp.listeners.XMPPMessageListener;
import co.chatsdk.xmpp.listeners.XMPPSubjectUpdatedListener;
import co.chatsdk.xmpp.module.XMPPModule;
import co.chatsdk.xmpp.ui.JidUtil;
import co.chatsdk.xmpp.utils.PresenceHelper;
import co.chatsdk.xmpp.utils.Role;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ben on 8/8/17.
 * https://github.com/igniterealtime/Smack/blob/master/documentation/extensions/muc.md
 */

public class XMPPMUCManager {

    private WeakReference<XMPPManager> manager;
    private MultiUserChatManager chatManager;

    protected HashMap<String, XMPPMUCUserStatusListener> userStatusListeners = new HashMap<>();
    protected HashMap<String, XMPPMUCMessageListener> messageListeners = new HashMap<>();

    private DisposableMap dm = new DisposableList();

    public XMPPMUCManager (XMPPManager manager_) {
        manager = new WeakReference<>(manager_);
        chatManager = MultiUserChatManager.getInstanceFor(manager.get().getConnection());
        chatManager.setAutoJoinOnReconnect(true);

        chatManager.setAutoJoinFailedCallback((muc, e) -> {
            ChatSDK.events().onError(e);
        });

        chatManager.addInvitationListener((conn, room, inviter, reason, password, message, invitation) -> {
            joinRoom(room, true).doOnSuccess(thread -> {
            }).ignoreElement().subscribe(ChatSDK.events());
        });
    }


    public Single<Thread> createRoom (final String name, final String description, final ArrayList<User> users) {
        return Single.defer(() -> {
            // Create a new group chat
            final String roomID = generateRoomId(name);

            final MultiUserChat chat = chatManager.getMultiUserChat(JidCreate.entityBareFrom(roomID));

            Resourcepart nickname = Resourcepart.from(name);

            chat.create(nickname);

            Form configurationForm = chat.getConfigurationForm();
            Form form = configurationForm.createAnswerForm();

            for(FormField f : form.getFields()) {
                Logger.debug(f.getVariable());
                if(f.getVariable().equals("muc#roomconfig_persistentroom")) {
                    form.setAnswer(f.getVariable(), true);
                }
                if(f.getVariable().equals("muc#roomconfig_publicroom")) {
                    form.setAnswer(f.getVariable(), false);
                }
                if(f.getVariable().equals("muc#roomconfig_maxusers")) {
                    List<String> list = new ArrayList<>();
                    list.add("200");
                    form.setAnswer(f.getVariable(), list);
                }
                if(f.getVariable().equals("muc#roomconfig_whois")) {
                    List<String> list = new ArrayList<>();
                    list.add("anyone");
                    form.setAnswer(f.getVariable(), list);
                }
                if(f.getVariable().equals("muc#roomconfig_membersonly")) {
                    form.setAnswer(f.getVariable(), true);
                }
                if(f.getVariable().equals("muc#roomconfig_moderatedroom")) {
                    form.setAnswer(f.getVariable(), true);
                }
                if(f.getVariable().equals("muc#roomconfig_roomname")) {
                    form.setAnswer(f.getVariable(), name);
                }
                if(f.getVariable().equals("muc#roomconfig_roomdesc")) {
                    form.setAnswer(f.getVariable(), description);
                }
                if(f.getVariable().equals("muc#roomconfig_roomowners")) {
                    ArrayList<String> owners = new ArrayList<>();
                    owners.add(ChatSDK.currentUserID());
                    form.setAnswer(f.getVariable(), owners);                    }
                if(f.getVariable().equals("muc#roomconfig_allowinvites")) {
                    form.setAnswer(f.getVariable(), true);
                }
            }

            // If this fails, we will jit the error block immediately...
            chat.sendConfigurationForm(form);

            // Add the users
            ArrayList<Completable> comp = new ArrayList<>();
            for(User user: users) {
                comp.add(inviteUser(user, chat));
            }

            return Completable.merge(comp).andThen(joinRoom(roomID));

        }).subscribeOn(Schedulers.io());
    }

    public Single<Thread> joinRoom (String roomJID) throws Exception {
        return joinRoom(getRoom(roomJID), true);
    }

    public Single<Thread> joinRoom (MultiUserChat chat, boolean bookmark) {
        return Single.defer(() -> {
            Thread thread = threadForRoomID(chat.getRoom().toString());

            Resourcepart nickname = nickname();
            if (nickname != null) {

                MucEnterConfiguration.Builder config = chat.getEnterConfigurationBuilder(nickname);

                // Work out how much history to request
                Date lastMessageDate = thread.lastMessageAddedDate();
                if (lastMessageDate != null) {
                    lastMessageDate = manager.get().clientToServerTime(lastMessageDate);
                    config.requestHistorySince(lastMessageDate);
                }
                else {
                    config.requestMaxStanzasHistory(XMPPModule.config().xmppMucMessageHistory);
                }

                XMPPMUCMessageListener chatMessageListener = getMessageListener(thread.getEntityID());
                if (chatMessageListener == null) {
                    chatMessageListener = new XMPPMUCMessageListener(XMPPMUCManager.this, chat);
                    putMessageListener(thread.getEntityID(), chatMessageListener);
                }

                dm.add(chatMessageListener);

                XMPPChatParticipantListener chatParticipantListener = new XMPPChatParticipantListener(XMPPMUCManager.this, chat);
                dm.add(chatParticipantListener);

                XMPPSubjectUpdatedListener subjectUpdatedListener = new XMPPSubjectUpdatedListener(chat);
                dm.add(subjectUpdatedListener);

                chat.addMessageListener(chatMessageListener);
                chat.addPresenceInterceptor(presence1 -> {
                    Logger.debug(presence1);
                });
                chat.addParticipantListener(chatParticipantListener);
                chat.addSubjectUpdatedListener(subjectUpdatedListener);

                XMPPMUCUserStatusListener userStatusListener = getUserStatusListener(thread.getEntityID());
                if (userStatusListener == null) {
                    userStatusListener = new XMPPMUCUserStatusListener(this, chat, thread);
                    putUserStatusListener(thread.getEntityID(), userStatusListener);
                }

                chat.addUserStatusListener(userStatusListener);


                chat.join(config.build());

                RoomInfo info = chatManager.getRoomInfo(chat.getRoom());
                String name = info.getName();

                thread.setName(name, false);

                if (bookmark) {
                    manager.get().bookmarkManager().addBookmarkedConference(name, chat.getRoom(), false, chat.getNickname(), null);
                }

                // Send presence
                Presence presence = PresenceHelper.presenceForUser(ChatSDK.currentUser());
                chat.changeAvailabilityStatus(presence.getStatus(), presence.getMode());

                thread.addUser(ChatSDK.currentUser());

                return userStatusListener.updateAffiliates().flatMap(affiliates -> {

                    List<Completable> completables = new ArrayList<>();

                    // Make sure we are subscribed to the members
                    for (Affiliate affiliate: affiliates) {
                        completables.add(ChatSDK.core().getUserForEntityID(affiliate.getJid().asBareJid().toString()).doOnSuccess(user -> {
                            if (affiliate.getAffiliation() == MUCAffiliation.owner) {
                                thread.setCreator(user);
                            }
                            thread.addUser(user);
                        }).ignoreElement());
                    }

                    return Completable.merge(completables).toSingle(() -> thread);
                });
            }
            return Single.error(new Throwable(ChatSDK.shared().getString(R.string.could_not_join_muc)));

        }).doOnSuccess(thread -> {
            ChatSDK.events().source().onNext(NetworkEvent.threadAdded(thread));
        }).subscribeOn(Schedulers.io());
    }

    public Single<String> getRoomName (MultiUserChat chat) {
        return Single.create((SingleOnSubscribe<String>) emitter -> {
            RoomInfo info = chatManager.getRoomInfo(chat.getRoom());
            String name = info.getName();
            emitter.onSuccess(name);

        }).subscribeOn(Schedulers.io());
    }

    public Resourcepart nickname() {
        try {
            Jid jid = JidCreate.entityBareFrom(ChatSDK.currentUserID());
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
                ChatSDK.thread().sendLocalSystemMessage(ChatSDK.shared().getString(R.string.room_no_longer_active), thread);
                // TODO: Handle this
            } catch (Exception e) {}
        }
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

    public MultiUserChat getRoom(String jid) throws Exception {
        return chatManager.getMultiUserChat(JidCreate.entityBareFrom(jid));
    }

    public Completable inviteUser(final User user, MultiUserChat chat) {
        return Completable.create(e -> {
            Jid jid = JidUtil.fromEntityID(user.getEntityID());
            chat.invite(JidCreate.entityBareFrom(user.getEntityID()), "");
            chat.grantMembership(jid);
            e.onComplete();
        }).subscribeOn(Schedulers.io());
    }

    private String generateRoomId(String name) throws Exception {
        String domain = mucServiceDomain();
        DateFormat format = new SimpleDateFormat("mm.dd.yy-HHmmss", CurrentLocale.get());
        return name.replaceAll("[^a-zA-Z0-9]", "") + "." + format.format(new Date()) + "@" + domain;
    }

    public String mucServiceDomain() throws Exception {
        List<DomainBareJid> services = chatManager.getMucServiceDomains();
        if(services.size() > 0) {
            return services.get(0).toString();
        } else {
            throw new Exception(ChatSDK.shared().getString(R.string.no_muc_service_available));
        }
    }

    public Thread threadForRoomID (MultiUserChat chat) {
        return threadForRoomID(chat.getRoom().toString());
    }

    public Thread threadForRoomID (String roomJID) {
        Logger.debug("Thread For Room " + roomJID);
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(roomJID);
        if(thread == null) {
            thread = ChatSDK.db().createEntity(Thread.class);

            thread.setEntityID(roomJID);

            thread.setCreator(ChatSDK.currentUser());
            thread.setCreationDate(new Date());
            thread.setType(ThreadType.PrivateGroup);
            thread.update();
            ChatSDK.events().source().onNext(NetworkEvent.threadAdded(thread));
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
            return null;
        }
    }

    public void dispose () {
        dm.dispose();
    }

    public Affiliate getAffiliateForUser(Thread thread, User user) {
        for (Affiliate affiliate : getAffiliates(thread)) {
            if (affiliate.getJid().asBareJid().toString().equals(user.getEntityID())) {
                return affiliate;
            }
        }
        return null;
    }

    protected List<Affiliate> getAffiliates(Thread thread) {
        XMPPMUCUserStatusListener listener = getUserStatusListener(thread.getEntityID());
        if (listener != null) {
            return listener.getAffiliates();
        }
        return new ArrayList<>();
    }

    public Single<List<Affiliate>> requestAffiliatesFromServer(Thread thread) {
        return Single.create((SingleOnSubscribe<List<Affiliate>>)emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            emitter.onSuccess(getAffiliates(chat));
        }).subscribeOn(Schedulers.io());
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
                ChatSDK.events().onError(e);
            }
        }
        return affiliates;
    }

    public int getRoleForUser(Thread thread, User user) {
        return Role.fromAffiliate(getAffiliateForUser(thread, user));
    }

//    public Completable setRole(Thread thread, User user, MUCRole role) {
//        return Completable.create(emitter -> {
//            MultiUserChat chat = chatForThreadID(thread.getEntityID());
//            Affiliate affiliate = getAffiliateForUser(thread, user);
//            if (chat != null && affiliate != null && affiliate.getRole() != role) {
//                if (role == MUCRole.moderator) {
//                    chat.grantModerator(affiliate.getNick());
//                }
//                if (role == MUCRole.participant) {
//                    chat.revokeModerator(affiliate.getNick());
//                    chat.grantVoice(affiliate.getNick());
//                }
//                else {
//                    chat.revokeVoice(affiliate.getNick());
//                }
//            }
//            emitter.onComplete();
//        }).subscribeOn(Schedulers.io());
//    }

    public Completable grantModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Affiliate affiliate = getAffiliateForUser(thread, user);
            if (chat != null && affiliate != null) {
                if (affiliate.getRole() != MUCRole.moderator) {
                    chat.grantModerator(affiliate.getNick());
                    emitter.onComplete();
                } else {
                    emitter.onComplete();
                }
            } else {
                emitter.onError(new Throwable(ChatSDK.shared().getString(R.string.permission_denied)));
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable revokeModerator(Thread thread, User user) {
        return Completable.create(emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Affiliate affiliate = getAffiliateForUser(thread, user);
            if (chat != null && affiliate != null) {
                if (affiliate.getRole() == MUCRole.moderator) {
                    chat.revokeModerator(affiliate.getNick());
                    emitter.onComplete();
                } else {
                    emitter.onComplete();
                }
            } else {
                emitter.onError(new Throwable(ChatSDK.shared().getString(R.string.permission_denied)));
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable grantVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Affiliate affiliate = getAffiliateForUser(thread, user);
            if (chat != null && affiliate != null) {
                chat.grantVoice(affiliate.getNick());
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(ChatSDK.shared().getString(R.string.permission_denied)));
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable revokeVoice(Thread thread, User user) {
        return Completable.create(emitter -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Affiliate affiliate = getAffiliateForUser(thread, user);
            if (chat != null && affiliate != null) {
                chat.revokeVoice(affiliate.getNick());
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(ChatSDK.shared().getString(R.string.permission_denied)));
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable setRole(Thread thread, User user, MUCAffiliation affiliation) {
        return Completable.defer(() -> {
            MultiUserChat chat = chatForThreadID(thread.getEntityID());
            Affiliate affiliate = getAffiliateForUser(thread, user);
            if (chat != null && affiliate != null && affiliate.getAffiliation() != affiliation) {
                if (affiliation == MUCAffiliation.owner) {
                    chat.grantOwnership(affiliate.getJid());
                }
                else if (affiliation == MUCAffiliation.admin) {
                    chat.grantAdmin(affiliate.getJid());
                }
                else if (affiliation == MUCAffiliation.member) {
                    chat.grantMembership(affiliate.getJid());
                }
                else {
                    chat.revokeMembership(affiliate.getJid());
                }
            }
            return refreshRoomAffiliation(chat);
        }).subscribeOn(Schedulers.io());
    }

    public Completable refreshRoomAffiliation(MultiUserChat chat) {
        return Completable.defer(() -> {
            XMPPMUCUserStatusListener listener = getUserStatusListener(chat.getRoom().toString());
            if (listener != null) {
                return listener.updateAffiliates().ignoreElement();
            }
            return Completable.complete();
        }).subscribeOn(Schedulers.io());
    }

    public XMPPMUCUserStatusListener getUserStatusListener(String threadEntityID) {
        return userStatusListeners.get(threadEntityID);
    }

    public void putUserStatusListener(String threadEntityID, XMPPMUCUserStatusListener listener) {
        userStatusListeners.put(threadEntityID, listener);
    }

    public XMPPMUCMessageListener getMessageListener(String threadEntityID) {
        return messageListeners.get(threadEntityID);
    }

    public void putMessageListener(String threadEntityID, XMPPMUCMessageListener listener) {
        messageListeners.put(threadEntityID, listener);
    }
}
