package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;

import app.xmpp.adapter.XMPPMUCManager;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.XMPPMessageParser;
import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;


/**
 * Created by ben on 8/25/17.
 */

public class XMPPMUCMessageListener implements MessageListener, Disposable {

    private WeakReference<XMPPMUCManager> parent;
    private WeakReference<MultiUserChat> chat;
    protected boolean disposed = false;

    public XMPPMUCMessageListener(XMPPMUCManager parent, MultiUserChat chat) {
        this.chat = new WeakReference<>(chat);
        this.parent = new WeakReference<>(parent);
        addListeners();
    }

    @Override
    public void processMessage(Message xmppMessage) {
        XMPPMessageWrapper xmr = new XMPPMessageWrapper(xmppMessage);
        if (xmr.isSilent()) {
            return;
        }

        if (xmppMessage.getFrom().equals(chat.get().getRoom())) {
            // This is probably the subject
            Logger.debug("Subject?");
        } else {
            String from = parent.get().userJID(chat.get(), xmppMessage.getFrom());

            Logger.debug("Message: " + xmppMessage.getBody());

            // is this a read receipt
            boolean isReadExtension = xmppMessage.getExtension(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace) != null;
            boolean isChatState = xmppMessage.getExtension(ChatStateExtension.NAMESPACE) != null;
            if (isReadExtension) {
                Logger.debug("MUC: isReadExtension");
            } else if(isChatState) {
                User user = ChatSDK.db().fetchUserWithEntityID(from);
                XMPPManager.shared().typingIndicatorManager.handleMessage(xmppMessage, user);
            } else {
                Thread thread = parent.get().threadForRoomID(chat.get());
                from can be null!
                XMPPMessageParser.addMessageToThread(thread, XMPPMessageWrapper.with(xmppMessage), from);
            }
        }
    }

    @Override
    public void dispose() {
        if (!disposed) {
            removeListeners();
            disposed = true;
        }
    }

    public void removeListeners() {
        MultiUserChat chat = this.chat.get();
        if (chat != null) {
            chat.removeMessageListener(this);
        }
    }

    public void addListeners() {
        MultiUserChat chat = this.chat.get();
        if (chat != null) {
            removeListeners();
            chat.addMessageListener(this);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
