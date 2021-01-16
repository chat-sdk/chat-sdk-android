package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.Jid;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPMUCManager;
import io.reactivex.Single;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.guru.common.RX;

public class XMPPMUCUserStatusListener implements UserStatusListener, PresenceListener {

    protected WeakReference<XMPPMUCManager> manager;
    protected WeakReference<MultiUserChat> chat;
    protected WeakReference<Thread> thread;

    protected List<Affiliate> affiliates = new ArrayList<>();

    public XMPPMUCUserStatusListener(XMPPMUCManager manager, MultiUserChat chat, Thread thread) {
        this.chat = new WeakReference<>(chat);
        this.thread = new WeakReference<>(thread);
        this.manager = new WeakReference<>(manager);
    }

    protected Map<Jid, MUCAffiliation> affiliationHashMap = new HashMap<>();
    protected Map<Jid, MUCRole> roleHashMap = new HashMap<>();

    @Override
    public void kicked(Jid actor, String reason) {
        Logger.debug("kicked");
        triggerServerUpdate();
    }

    @Override
    public void voiceGranted() {
        Logger.debug("voice granted");
        triggerServerUpdate();
    }

    @Override
    public void voiceRevoked() {
        Logger.debug("voice revoked");
        triggerServerUpdate();
    }

    @Override
    public void banned(Jid actor, String reason) {
        Logger.debug("banned");
        triggerServerUpdate();
    }

    @Override
    public void membershipGranted() {
        Logger.debug("membershipGranted");
        triggerServerUpdate();
    }

    @Override
    public void membershipRevoked() {
        Logger.debug("membershipRevoked");
        triggerServerUpdate();
    }

    @Override
    public void moderatorGranted() {
        Logger.debug("moderatorGranted");
        triggerServerUpdate();
    }

    @Override
    public void moderatorRevoked() {
        Logger.debug("moderatorRevoked");
        triggerServerUpdate();
    }

    @Override
    public void ownershipGranted() {
        Logger.debug("ownershipGranted");
        triggerServerUpdate();
    }

    @Override
    public void ownershipRevoked() {
        Logger.debug("ownershipRevoked");
        triggerServerUpdate();
    }

    @Override
    public void adminGranted() {
        Logger.debug("adminGranted");
        triggerServerUpdate();
    }

    @Override
    public void adminRevoked() {
        Logger.debug("adminRevoked");
        triggerServerUpdate();
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
        return manager.get().requestAffiliatesFromServer(thread.get()).doOnSuccess(affiliates -> {
            this.affiliates.clear();
            this.affiliates.addAll(affiliates);
            updateMembershipMap();
//            ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread.get(), ChatSDK.currentUser()));
        }).subscribeOn(RX.io());
    }

    public void updateMembershipMap() {
        for (Affiliate affiliate: affiliates) {
            updateMembershipMap(affiliate.getJid(), affiliate.getAffiliation(), affiliate.getRole());
        }
    }

    public void updateMembershipMap(Jid jid, MUCAffiliation affiliation, MUCRole role) {
        MUCAffiliation oldAffiliation = affiliationHashMap.get(jid);
        MUCRole oldRole = roleHashMap.get(jid);
        boolean isCurrentUser = ChatSDK.currentUserID().equals(jid.toString());

        boolean changedAffiliation = false;
        if (oldAffiliation != affiliation) {
            changedAffiliation = true;
            if (isCurrentUser && oldAffiliation != null) {
                userChangedAffiliation();
            }
            affiliationHashMap.put(jid, affiliation);
        }
        boolean changedRole = false;
        if (oldRole != role) {
            changedRole = true;
            if (isCurrentUser && oldRole != null) {
                userChangedRole(role, oldRole);
            }
            roleHashMap.put(jid, role);
        }
        if (changedAffiliation || changedRole) {
            User user = ChatSDK.db().fetchUserWithEntityID(jid.toString());
            if (user != null) {
                ChatSDK.events().source().accept(NetworkEvent.threadUsersRoleChanged(thread.get(), user));
            }
        }
    }

    public void userChangedAffiliation() {
        try {
            String role = ChatSDK.thread().roleForUser(thread.get(), ChatSDK.currentUser());
            // My affiliation has changed
            String message = String.format(ChatSDK.getString(R.string.role_changed_to__), role);
            ChatSDK.thread().sendLocalSystemMessage(message, thread.get());
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public void userChangedRole(MUCRole newRole, MUCRole old) {
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
                ChatSDK.thread().sendLocalSystemMessage(message, thread.get());
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public List<Affiliate> getAffiliates() {
        return affiliates;
    }

    @Override
    public void processPresence(Presence presence) {
        ExtensionElement element = presence.getExtension("http://jabber.org/protocol/muc#user");
        if(element instanceof MUCUser && thread != null) {
            MUCUser user = (MUCUser) element;
            Jid jid = user.getItem().getJid().asBareJid();
            if (!jid.toString().equals(ChatSDK.currentUserID())) {
                updateMembershipMap(jid, user.getItem().getAffiliation(), user.getItem().getRole());
            }
        }

    }
}
