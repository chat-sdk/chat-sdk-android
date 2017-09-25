package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.Jid;

import java.lang.ref.WeakReference;
import java.util.Collection;

import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
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
        presenceEventSource.subscribeOn(Schedulers.single());
    }

    @Override
    public void entriesAdded(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            Timber.v("Added to roster " + jid.toString());
            manager.get().userManager.addContact(jid.asBareJid()).subscribe();
        }
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {
        for(Jid jid : addresses) {
            Timber.v("Updated in roster " + jid.toString());
        }
    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {
        for(Jid jid : addresses) {

            manager.get().userManager.deleteContact(jid.asBareJid().toString());
            Timber.v("Deleted from roster " + jid);
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        // Recommended to freshen the presence
        Presence freshPresence = manager.get().roster().getPresence(presence.getFrom().asBareJid());
        presenceEventSource.onNext(freshPresence);
    }

}
