package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jxmpp.jid.Jid;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.XMPPMUCManager;
import co.chatsdk.xmpp.utils.JidUtil;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class XMPPMUCUserStatusListener implements UserStatusListener {

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
            ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread.get(), ChatSDK.currentUser()));
        }).subscribeOn(Schedulers.io());
    }

    public void updateMembershipMap() {
        for (Affiliate affiliate: affiliates) {
            MUCAffiliation oldAffiliation = affiliationHashMap.get(affiliate.getJid());
            MUCRole oldRole = roleHashMap.get(affiliate.getJid());

            boolean changedAffiliation = false;
            if (oldAffiliation != affiliate.getAffiliation()) {
                changedAffiliation = true;
                userChangedAffiliation(affiliate, oldAffiliation);
                affiliationHashMap.put(affiliate.getJid(), affiliate.getAffiliation());
            }
            boolean changedRole = false;
            if (oldRole != affiliate.getRole()) {
                changedRole = true;
                userChangedRole(affiliate, oldRole);
                roleHashMap.put(affiliate.getJid(), affiliate.getRole());
            }
            if (changedAffiliation || changedRole) {
                User user = ChatSDK.db().fetchUserWithEntityID(affiliate.getJid().toString());
                if (user != null) {
                    ChatSDK.events().source().onNext(NetworkEvent.threadUsersRoleChanged(thread.get(), ChatSDK.currentUser()));
                }
            }
        }
    }

    public void userChangedAffiliation(Affiliate affiliate, MUCAffiliation old) {
        try {
            if (old == null) {
                return;
            }
            User currentUser = ChatSDK.currentUser();
            Jid myJid = JidUtil.fromEntityID(currentUser.getEntityID());
            if (affiliate.getJid().equals(myJid)) {
                String role = ChatSDK.thread().roleForUser(thread.get(), currentUser);
                // My affiliation has changed
                String message = String.format(ChatSDK.shared().getString(co.chatsdk.core.R.string.role_changed_to__), role);
                ChatSDK.thread().sendLocalSystemMessage(message, thread.get());
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public void userChangedRole(Affiliate affiliate, MUCRole old) {
        try {
            if (old == null) {
                return;
            }
            User currentUser = ChatSDK.currentUser();
            Jid myJid = JidUtil.fromEntityID(currentUser.getEntityID());
            if (affiliate.getJid().equals(myJid)) {
                String message = null;
                MUCRole newRole = affiliate.getRole();
                if (old == MUCRole.participant && newRole == MUCRole.moderator) {
                    message = String.format(ChatSDK.shared().getString(co.chatsdk.core.R.string.__granted), ChatSDK.shared().getString(R.string.moderator));
                }
                if (old == MUCRole.moderator && newRole == MUCRole.participant) {
                    message = String.format(ChatSDK.shared().getString(co.chatsdk.core.R.string.__revoked), ChatSDK.shared().getString(R.string.moderator));
                }
                if (old == MUCRole.visitor && newRole == MUCRole.participant) {
                    message = String.format(ChatSDK.shared().getString(co.chatsdk.core.R.string.__granted), ChatSDK.shared().getString(R.string.participant));
                }
                if (old == MUCRole.participant && newRole == MUCRole.visitor) {
                    message = String.format(ChatSDK.shared().getString(co.chatsdk.core.R.string.__revoked), ChatSDK.shared().getString(R.string.participant));
                }
                if (message != null) {
                    ChatSDK.thread().sendLocalSystemMessage(message, thread.get());
                }
            }
        } catch (Exception e) {
            ChatSDK.events().onError(e);
        }
    }

    public List<Affiliate> getAffiliates() {
        return affiliates;
    }
}
