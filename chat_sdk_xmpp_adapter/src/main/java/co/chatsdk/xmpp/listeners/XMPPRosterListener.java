package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.Collection;
import java.util.List;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BUser;
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

    public XMPPRosterListener () {
        // Clear down the contacts stored locally
        List<BUser> contacts = NM.contact().contacts();
        for(BUser user : contacts) {
            NM.contact().deleteContact(user, ConnectionType.Contact);
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        String name = presence.getFrom();
        // Recommended to freshen the presence
        Presence freshPresence = Roster.getInstanceFor(XMPPManager.shared().getConnection()).getPresence(name);
        presenceEventSource.onNext(freshPresence);
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        for(String jid : addresses) {
            Timber.v("Added to roster " + jid);
            XMPPManager.shared().userManager.updateUserFromVCard(new JID(jid)).subscribe(new BiConsumer<BUser, Throwable>() {
                @Override
                public void accept(BUser user, Throwable throwable) throws Exception {

                }
            });
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
        for(String entry : addresses) {
            Timber.v("Deleted from roster " + entry);
        }
    }

}
