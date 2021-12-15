package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.muc.packet.MUCItem;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPMUCManager;
import app.xmpp.adapter.module.XMPPModule;
import app.xmpp.adapter.utils.ActionMessageCache;
import app.xmpp.adapter.utils.Role;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public class XMPPMUCRoleListener implements UserStatusListener, PresenceListener, Disposable, MessageListener {

    protected WeakReference<XMPPMUCManager> manager;
    protected MultiUserChat chat;
    protected Thread thread;
    protected DisposableMap dm = new DisposableMap();
    protected boolean disposed = false;

    public XMPPMUCRoleListener(XMPPMUCManager manager, MultiUserChat chat, Thread thread) {
        this.chat = chat;
        this.thread = thread;
        this.manager = new WeakReference<>(manager);

        addListeners();
    }

    protected Map<Jid, MUCAffiliation> affiliationMap = new HashMap<>();
    protected Map<Jid, MUCRole> roleMap = new HashMap<>();
    protected Map<Jid, Resourcepart> nickMap = new HashMap<>();

    @Override
    public void kicked(Jid actor, String reason) {
        Logger.debug("kicked");
//        triggerServerUpdate();
    }

    @Override
    public void voiceGranted() {
        Logger.debug("voice granted");
//        triggerServerUpdate();
    }

    @Override
    public void voiceRevoked() {
        Logger.debug("voice revoked");
//        triggerServerUpdate();
    }

    @Override
    public void banned(Jid actor, String reason) {
        Logger.debug("banned");
//        triggerServerUpdate();
    }

    @Override
    public void membershipGranted() {
        Logger.debug("membershipGranted");
//        triggerServerUpdate();
    }

    @Override
    public void membershipRevoked() {
        Logger.debug("membershipRevoked");
//        triggerServerUpdate();
    }

    @Override
    public void moderatorGranted() {
        Logger.debug("moderatorGranted");
//        triggerServerUpdate();
    }

    @Override
    public void moderatorRevoked() {
        Logger.debug("moderatorRevoked");
//        triggerServerUpdate();
    }

    @Override
    public void ownershipGranted() {
        Logger.debug("ownershipGranted");
//        triggerServerUpdate();
    }

    @Override
    public void ownershipRevoked() {
        Logger.debug("ownershipRevoked");
//        triggerServerUpdate();
    }

    @Override
    public void adminGranted() {
        Logger.debug("adminGranted");
//        triggerServerUpdate();
    }

    @Override
    public void adminRevoked() {
        Logger.debug("adminRevoked");
//        triggerServerUpdate();
    }

    @Override
    public void roomDestroyed(MultiUserChat alternateMUC, String reason) {
        Logger.debug("roomDestroyed");
    }

    public void triggerServerUpdate() {
        updateAffiliates().ignoreElement().subscribe(ChatSDK.events());
    }

    /**
     * This can take some time so cache the results and only update when necessary
     */
    public Single<List<Affiliate>> updateAffiliates() {
        return manager.get().requestAffiliatesFromServer(thread).doOnSuccess(affiliates -> {
            updateMembershipMap(affiliates);

            for(User user: thread.getUsers()) {
                MUCAffiliation affiliation = affiliationMap.get(JidCreate.bareFrom(user.getEntityID()));
                boolean hasLeft = affiliation == null || affiliation == MUCAffiliation.outcast || affiliation == MUCAffiliation.none;

                UserThreadLink link = thread.getUserThreadLink(user.getId());
                if (link != null) {
                    link.setHasLeft(hasLeft);
                }
            }

//            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread, ChatSDK.currentUser()));
        }).subscribeOn(RX.io());
    }

    public MUCAffiliation getAffiliation(Jid jid) {
        return affiliationMap.get(jid);
    }

    public MUCAffiliation getAffiliation(User user) {
        try {
            return getAffiliation(JidCreate.bareFrom(user.getEntityID()));
        } catch (Exception e) {
            return MUCAffiliation.none;
        }
    }

    public MUCRole getRole(Jid jid) {
        return roleMap.get(jid);
    }

    public MUCRole getRole(User user) {
        try {
            return getRole(JidCreate.bareFrom(user.getEntityID()));
        } catch (Exception e) {
            return MUCRole.none;
        }
    }

    public Resourcepart getNick(Jid jid) {
        return nickMap.get(jid);
    }

    public Resourcepart getNick(User user) {
        try {
            return getNick(JidCreate.bareFrom(user.getEntityID()));
        } catch (Exception e) {
            return null;
        }
    }

    public void updateMembershipMap(List<Affiliate> affiliates) {
        Iterator<Affiliate> iterator = affiliates.iterator();

        affiliationMap.clear();
        roleMap.clear();

        while(iterator.hasNext()) {
            Affiliate a = iterator.next();
            updateMembershipMap(a.getJid(), a.getNick(), a.getAffiliation(), a.getRole());
        }
    }

    public void updateMembershipMap(Jid jid, Resourcepart nick, MUCAffiliation affiliation, MUCRole role) {
        affiliationMap.put(jid, affiliation);
        roleMap.put(jid, role);
        nickMap.put(jid, nick);

        User user = ChatSDK.core().getUserNowForEntityID(jid.toString());
//        if (role != null && role != MUCRole.none) {
            thread.addUser(user);
//        }

        UserThreadLink link = thread.getUserThreadLink(user.getId());

        boolean didJoin = false;
        if (role != null && role != MUCRole.none) {
            didJoin = link.setHasLeft(false);
        }

        String oldAffiliation = link.getAffiliation();
        String oldRole = link.getRole();

        boolean isMe = user.isMe();

        boolean affiliationChanged = false;
        if (affiliation != null && !affiliation.name().equals(oldAffiliation)) {
            link.setAffiliation(affiliation.name());

            if (affiliation == MUCAffiliation.owner && thread.getCreator() == null) {
                thread.setCreator(user);
            }

            affiliationChanged = true;
            if (isMe && oldAffiliation != null) {
                sendLocalMessageForAffiliationChange(oldAffiliation);
            }
        }

        boolean roleChanged = false;
        if (role != null && !role.name().equals(oldRole)) {
            link.setRole(role.name());

            roleChanged = true;
            if (isMe && oldRole != null) {
                sendLocalMessageForRoleChange(role, roleMap.get(jid));
            }
        }
        if (affiliationChanged || roleChanged || didJoin) {
            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleUpdated(thread, user));
        }
    }

    public void sendLocalMessageForAffiliationChange(String oldAffiliation) {
        try {
            String role = ChatSDK.thread().roleForUser(thread, ChatSDK.currentUser());
            // My affiliation has changed
            if (Role.isOutcast(role) || Role.isOutcast(oldAffiliation) || XMPPModule.config().sendSystemMessageForAffiliationChange) {
                String message = String.format(ChatSDK.getString(R.string.role_changed_to__), role);
                ChatSDK.thread().sendLocalSystemMessage(message, thread);
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public void sendLocalMessageForRoleChange(MUCRole newRole, MUCRole old) {
        try {
            String message = null;
            if (old == MUCRole.participant && newRole == MUCRole.moderator) {
                message = String.format(ChatSDK.getString(R.string.__granted), ChatSDK.getString(R.string.moderator));
            }
            if (old == MUCRole.moderator && newRole == MUCRole.participant) {
                message = String.format(ChatSDK.getString(R.string.__revoked), ChatSDK.getString(R.string.moderator));
            }
            if (old == MUCRole.visitor && newRole == MUCRole.participant) {
                message = String.format(ChatSDK.getString(R.string.__granted), ChatSDK.getString(R.string.participant));
            }
            if (old == MUCRole.participant && newRole == MUCRole.visitor) {
                message = String.format(ChatSDK.getString(R.string.__revoked), ChatSDK.getString(R.string.participant));
            }
            if (message != null) {
                if (newRole != MUCRole.none && XMPPModule.config().sendSystemMessageForRoleChange) {
                    ChatSDK.thread().sendLocalSystemMessage(message, thread);
                }
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    @Override
    public void processPresence(Presence presence) {

        ExtensionElement element = presence.getExtension("http://jabber.org/protocol/muc#user");
        if(element instanceof MUCUser && thread != null) {
            MUCItem item = ((MUCUser) element).getItem();

            Jid jid = item.getJid().asBareJid();
            String entityID = jid.toString();

            MUCAffiliation affiliation = item.getAffiliation();
            MUCRole role = item.getRole();
            Resourcepart nick = item.getNick();

            // Full JID
            Occupant occupant = chat.getOccupant(presence.getFrom().asEntityFullJidIfPossible());
            if (occupant != null) {
                if (nick == null) {
                    nick = occupant.getNick();
                }
                if (role == null) {
                    role = occupant.getRole();
                }
                if (affiliation == null) {
                    affiliation = occupant.getAffiliation();
                }
            }

            updateMembershipMap(jid, nick, affiliation, role);

            User user = ChatSDK.core().getUserNowForEntityID(entityID);
            UserThreadLink link = thread.getUserThreadLink(user.getId());

            boolean updated = false;
            boolean joined = false;

            if (presence.getType() == Presence.Type.available) {
                joined = link.setIsActive(true);
                updated = link.setHasLeft(false) || updated;
            }
            if (presence.getType() == Presence.Type.unavailable) {
                updated = link.setIsActive(false) || updated;
            }

            if (joined) {
                ChatSDK.events().source().accept(NetworkEvent.threadUserAdded(thread, user));
            } else if (updated) {
                ChatSDK.events().source().accept(NetworkEvent.userPresenceUpdated(user));
            }

            if(entityID.equals(ChatSDK.currentUserID()) && affiliation == MUCAffiliation.outcast || presence.getType() == Presence.Type.unavailable) {
                manager.get().deactivateThread(thread);
            }

        }

    }

    @Override
    public void dispose() {
        if (!disposed) {
            removeListeners();
            dm.dispose();
            disposed = true;
        }
    }

    public void removeListeners() {
        if (chat != null) {
            chat.removePresenceInterceptor(this);
            chat.removeUserStatusListener(this);
            chat.removeParticipantListener(this);
            chat.removeMessageListener(this);
        }
    }

    public void addListeners() {
        if (chat != null) {
            removeListeners();
            chat.addPresenceInterceptor(this);
            chat.addUserStatusListener(this);
            chat.addParticipantListener(this);
            chat.addMessageListener(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void processMessage(Message message) {
        XMPPMessageWrapper xm = new XMPPMessageWrapper(message);
        if (xm.hasAction(MessageType.Action.UserLeftGroup) && !ActionMessageCache.shouldIgnore(message.getStanzaId())) {

            ActionMessageCache.addMessageToIgnore(message.getStanzaId());

            String from = xm.userEntityID();
            if (from != null) {
                User user = ChatSDK.core().getUserNowForEntityID(from);

                if (!user.isMe()) {
                    UserThreadLink link = thread.getUserThreadLink(user.getId());
                    if (link.setHasLeft(true)) {
                        ChatSDK.events().source().accept(NetworkEvent.threadUserRemoved(thread, user));
                    }
                }
            }
        }
    }
}
