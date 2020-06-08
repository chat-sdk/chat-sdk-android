package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;
import java.util.Collection;

import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import app.xmpp.adapter.XMPPManager;
import io.reactivex.Observable;
import sdk.guru.common.RX;
import io.reactivex.subjects.PublishSubject;


/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPRosterListener implements RosterListener {

    protected PublishSubject<Presence> presenceEventSource = PublishSubject.create();
    private WeakReference<XMPPManager> manager;

    public XMPPRosterListener (XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
    }

    @Override
    public void entriesAdded(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            Logger.debug("Added to roster " + jid.toString());
            manager.get().userManager.addContact(jid.asBareJid()).subscribe(ChatSDK.events());
        }
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            Logger.debug("Updated in roster " + jid.toString());
        }
    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            User user = ChatSDK.db().fetchUserWithEntityID(jid.asBareJid().toString());
            ChatSDK.contact().deleteContact(user, ConnectionType.Contact).subscribe(ChatSDK.events());
            Logger.debug("Deleted from roster " + jid);
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        // Recommended to freshen the presence
        Presence freshPresence = manager.get().roster().getPresence(presence.getFrom().asBareJid());
        presenceEventSource.onNext(freshPresence);
    }

    public Observable<Presence> getPresenceEvents() {
        return presenceEventSource.subscribeOn(RX.io()).hide();
    }
}
