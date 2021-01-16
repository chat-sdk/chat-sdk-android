package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.Jid;

import java.lang.ref.WeakReference;

import app.xmpp.adapter.XMPPMUCManager;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;

/**
 * Created by ben on 8/25/17.
 */

public class XMPPChatParticipantListener implements PresenceListener, Disposable {

    private Disposable disposable;
    private WeakReference<XMPPMUCManager> parent;
    private WeakReference<MultiUserChat> chat;

    public XMPPChatParticipantListener (XMPPMUCManager parent, MultiUserChat chat) {
        this.parent = new WeakReference<>(parent);
        this.chat = new WeakReference<>(chat);
    }

    @Override
    public void processPresence(Presence presence) {
        // Here we need to add the users to the lookup so we can
        // get from the user's room JID to their real JID
        Thread thread = parent.get().threadForRoomID(chat.get().getRoom().toString());

        ExtensionElement element = presence.getExtension("http://jabber.org/protocol/muc#user");
        if(element instanceof MUCUser && thread != null) {
            MUCUser userElement = (MUCUser) element;
            Jid userJID = userElement.getItem().getJid();
            ChatSDK.events().disposeOnLogout(ChatSDK.core().getUserForEntityID(userJID.asBareJid().toString()).subscribe(thread::addUser, ChatSDK.events()));
        }
    }

    @Override
    public void dispose() {
        chat.get().removeParticipantListener(this);
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        if (disposable != null) {
            return disposable.isDisposed();
        }
        return true;
    }
}
