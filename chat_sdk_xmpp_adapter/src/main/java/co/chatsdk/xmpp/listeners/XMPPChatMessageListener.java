package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.lang.ref.WeakReference;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.utils.IsDisposable;
import co.chatsdk.xmpp.XMPPMUCManager;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.XMPPMessageParser;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import timber.log.Timber;

/**
 * Created by ben on 8/25/17.
 */

public class XMPPChatMessageListener implements MessageListener, IsDisposable {

    private WeakReference<XMPPMUCManager> parent;
    private WeakReference<MultiUserChat> chat;
    private Disposable disposable;

    public XMPPChatMessageListener (XMPPMUCManager parent, MultiUserChat chat) {
        this.chat = new WeakReference<>(chat);
        this.parent = new WeakReference<XMPPMUCManager>(parent);
    }

    @Override
    public void processMessage(Message xmppMessage) {
        String userJID = parent.get().userJID(chat.get(), xmppMessage.getFrom().asBareJid().toString());
        Timber.d("Message: " + xmppMessage.getBody());
        disposable = XMPPMessageParser.parse(xmppMessage, userJID).subscribe(new BiConsumer<co.chatsdk.core.dao.Message, Throwable>() {
            @Override
            public void accept(co.chatsdk.core.dao.Message message, Throwable throwable) throws Exception {
                if(message != null) {
                    NetworkEvent event = NetworkEvent.messageAdded(message.getThread(), message);
                    NM.events().source().onNext(event);
                }
            }
        });
        ChatStateExtension chatState = (ChatStateExtension) xmppMessage.getExtension(ChatStateExtension.NAMESPACE);
        if(chatState != null) {
            User user = StorageManager.shared().fetchUserWithEntityID(userJID);
            XMPPManager.shared().typingIndicatorManager.handleMessage(xmppMessage, user);
            Timber.v("Chat State: " + chatState.getChatState());
        }

    }

    @Override
    public void dispose() {
        chat.get().removeMessageListener(this);
        disposable.dispose();
    }
}
