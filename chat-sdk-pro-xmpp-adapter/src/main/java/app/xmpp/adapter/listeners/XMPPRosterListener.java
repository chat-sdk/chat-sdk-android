package app.xmpp.adapter.listeners;

import com.jakewharton.rxrelay2.PublishRelay;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.module.XMPPModule;
import io.reactivex.Observable;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.guru.common.RX;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPRosterListener implements RosterListener {

    protected PublishRelay<Presence> presenceEventSource = PublishRelay.create();
    private WeakReference<XMPPManager> manager;

    public XMPPRosterListener (XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
    }

    @Override
    public void entriesAdded(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            Logger.debug("Added to roster " + jid.toString());

            BareJid bare = jid.asBareJid();

            // Only add as a contact if this is actually a contact...
            RosterEntry entry = manager.get().roster().getEntry(bare);

            Map<String, Object> data = new HashMap<>();
            if (entry != null) {
                data.put(XMPPManager.xmppRosterEntry, entry);
            }
            data.put(XMPPManager.xmppRosterJID, jid);
            data.put(XMPPManager.xmppRosterEventType, XMPPManager.xmppRosterItemAdded);

            ChatSDK.hook().executeHook(XMPPManager.xmppRosterItemAdded, data).subscribe();

            if (XMPPModule.shared().config.reciprocalPresenceRequests && (entry.getType() == RosterPacket.ItemType.to || entry.getType() == RosterPacket.ItemType.both)) {
                manager.get().userManager.addContact(bare).subscribe(ChatSDK.events());
            } else {                try {
                    manager.get().roster().preApprove(bare);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            RosterEntry entry = manager.get().roster().getEntry(jid.asBareJid());

            entry.canSeeHisPresence();

            Map<String, Object> data = new HashMap<>();
            if (entry != null) {
                data.put(XMPPManager.xmppRosterEntry, entry);
            }
            data.put(XMPPManager.xmppRosterJID, jid);
            data.put(XMPPManager.xmppRosterEventType, XMPPManager.xmppRosterItemUpdated);

            ChatSDK.hook().executeHook(XMPPManager.xmppRosterItemUpdated, data).subscribe();
            Logger.debug("Updated in roster " + jid.toString());
        }
    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {
        for(Jid jid : addresses) {

            RosterEntry entry = manager.get().roster().getEntry(jid.asBareJid());
            Map<String, Object> data = new HashMap<>();
            if (entry != null) {
                data.put(XMPPManager.xmppRosterEntry, entry);
            }
            data.put(XMPPManager.xmppRosterJID, jid);
            data.put(XMPPManager.xmppRosterEventType, XMPPManager.xmppRosterItemRemoved);

            ChatSDK.hook().executeHook(XMPPManager.xmppRosterItemRemoved, data).subscribe();

            if (entry != null && (entry.getType() == RosterPacket.ItemType.to || entry.getType() == RosterPacket.ItemType.both)) {
                User user = ChatSDK.db().fetchUserWithEntityID(jid.asBareJid().toString());
                ChatSDK.contact().deleteContact(user, ConnectionType.Contact).subscribe(ChatSDK.events());
                Logger.debug("Deleted from roster " + jid);
            }
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        // Recommended to freshen the presence
        Presence freshPresence = manager.get().roster().getPresence(presence.getFrom().asBareJid());
        presenceEventSource.accept(freshPresence);
    }

    public Observable<Presence> getPresenceEvents() {
        return presenceEventSource.subscribeOn(RX.io()).hide();
    }
}
