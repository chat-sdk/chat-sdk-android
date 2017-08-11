package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.JID;
import io.reactivex.functions.BiConsumer;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPRosterListener implements RosterListener {

    public PublishSubject<Presence> presenceEventSource = PublishSubject.create();
    private WeakReference<XMPPManager> manager;

    public XMPPRosterListener (XMPPManager manager) {
        this.manager = new WeakReference<>(manager);
        // Clear down the contacts stored locally
    }

    @Override
    public void presenceChanged(Presence presence) {
        String name = presence.getFrom();
        // Recommended to freshen the presence
        Presence freshPresence = Roster.getInstanceFor(manager.get().getConnection()).getPresence(name);
        presenceEventSource.onNext(freshPresence);
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        for(String jid : addresses) {
            Timber.v("Added to roster " + jid);
            manager.get().userManager.addContact(jid).subscribe();
        }
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        for(String entry : addresses) {
            Timber.v("Updated in roster " + entry);
        }
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        for(String jid : addresses) {
            manager.get().userManager.deleteContact(jid);
            Timber.v("Deleted from roster " + jid);
        }
    }

}
