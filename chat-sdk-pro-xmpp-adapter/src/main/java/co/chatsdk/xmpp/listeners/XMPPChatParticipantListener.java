package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.Jid;

import java.lang.ref.WeakReference;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.xmpp.XMPPMUCManager;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.disposables.Disposable;

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
//        ExtensionElement element = presence.getExtension();
        Jid userJID = null;

        ExtensionElement element = presence.getExtension("http://jabber.org/protocol/muc#user");
        if(element instanceof MUCUser) {
            MUCUser userElement = (MUCUser) element;
            userJID = userElement.getItem().getJid();
        }

        Thread thread = parent.get().threadForRoomID(chat.get().getRoom().toString());

        if (userJID != null) {
            ChatSDK.events().disposeOnLogout(ChatSDK.core().getUserForEntityID(userJID.asBareJid().toString()).subscribe(thread::addUser, ChatSDK.events()));
        }

//
//        User user = ChatSDK.db().fetchUserWithEntityID(userJID.asBareJid().toString());
//        if(thread != null && user != null) {
//            thread.addUser(user);
//        }
//
//        try {
//            disposable = XMPPManager.shared().userManager.updateUserFromVCard(userJID).subscribe((user1, throwable) -> {
//
//            });
//        }
//        catch (Exception e) {
//            ChatSDK.events().onError(e);
//        }

//        parent.get().addUserToLookup(chat.get(), presence.getFrom(), userJID.asBareJid());
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
