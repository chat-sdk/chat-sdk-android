package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.events.NetworkEvent;

/**
 * Created by benjaminsmiley-andrews on 04/07/2017.
 */

public class XMPPChatMessageListener implements ChatMessageListener {

    @Override
    public void processMessage(Chat chat, Message xmppMessage) {
        BThread thread = StorageManager.shared().fetchOrCreateEntityWithEntityID(BThread.class, chat.getThreadID());
        BMessage message = StorageManager.shared().fetchOrCreateEntityWithEntityID(BMessage.class, xmppMessage.getStanzaId());
        message.setText(xmppMessage.getBody());
        message.setThread(thread);
        message.update();

        NetworkEvent event = NetworkEvent.messageAdded(thread, message);
        NM.events().source().onNext(event);
    }

}
