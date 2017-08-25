package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;

import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.utils.IsDisposable;
import co.chatsdk.xmpp.XMPPMUCManager;
import co.chatsdk.xmpp.XMPPManager;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 8/25/17.
 */

public class XMPPChatParticipantListener implements PresenceListener, IsDisposable {

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
        ExtensionElement element = presence.getExtension("http://jabber.org/protocol/muc#user");
        String jid = null;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(element.toXML().toString()));

//                            parser.setInput(new StringReader("<foo>Hello World!</foo>"));
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                if(tagname != null && tagname.equalsIgnoreCase("item")) {
                    jid = parser.getAttributeValue(null, "jid");
                    break;
                }
                parser.next();
            }

            Jid userJID = JidCreate.bareFrom(jid);

            Thread thread = parent.get().threadForRoomID(chat.get().getRoom().toString());
            User user = StorageManager.shared().fetchUserWithEntityID(userJID.asBareJid().toString());
            if(thread != null && user != null) {
                thread.addUser(user);
            }

            try {
                disposable = XMPPManager.shared().userManager.updateUserFromVCard(userJID).subscribe(new BiConsumer<User, Throwable>() {
                    @Override
                    public void accept(User user, Throwable throwable) throws Exception {

                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            parser.getAttributeCount();
        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        if(jid != null) {
            try {
                parent.get().addUserToLookup(chat.get(), presence.getFrom().asBareJid().toString(), JidCreate.bareFrom(jid).toString());
            }
            catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        chat.get().removeParticipantListener(this);
        disposable.dispose();
    }
}
