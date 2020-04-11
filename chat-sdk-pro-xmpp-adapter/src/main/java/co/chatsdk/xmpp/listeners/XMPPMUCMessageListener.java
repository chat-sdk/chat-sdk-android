package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.pmw.tinylog.Logger;

import java.lang.ref.WeakReference;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.xmpp.XMPPMUCManager;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.XMPPMessageParser;
import co.chatsdk.xmpp.utils.XMPPMessageWrapper;
import io.reactivex.disposables.Disposable;


/**
 * Created by ben on 8/25/17.
 */

public class XMPPMUCMessageListener implements MessageListener, Disposable {

    private WeakReference<XMPPMUCManager> parent;
    private WeakReference<MultiUserChat> chat;
    private Disposable disposable;

    public XMPPMUCMessageListener(XMPPMUCManager parent, MultiUserChat chat) {
        this.chat = new WeakReference<>(chat);
        this.parent = new WeakReference<>(parent);
    }

    @Override
    public void processMessage(Message xmppMessage) {
        String from = parent.get().userJID(chat.get(), xmppMessage.getFrom());

        Logger.debug("Message: " + xmppMessage.getBody());

        Thread thread = parent.get().threadForRoomID(chat.get());

        ChatStateExtension chatState = (ChatStateExtension) xmppMessage.getExtension(ChatStateExtension.NAMESPACE);
        if(chatState != null) {
            User user = ChatSDK.db().fetchUserWithEntityID(from);
            XMPPManager.shared().typingIndicatorManager.handleMessage(xmppMessage, user);
            Logger.debug("Chat State: " + chatState.getChatState());
        } else {
            XMPPMessageParser.addMessageToThread(thread, XMPPMessageWrapper.with(xmppMessage), from);
        }

    }

    @Override
    public void dispose() {
        chat.get().removeMessageListener(this);
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
