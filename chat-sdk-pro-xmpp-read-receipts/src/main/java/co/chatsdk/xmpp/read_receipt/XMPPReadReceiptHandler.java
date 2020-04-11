package co.chatsdk.xmpp.read_receipt;

import org.jivesoftware.smack.packet.Stanza;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.xmpp.XMPPManager;

public class XMPPReadReceiptHandler implements ReadReceiptHandler {

    @Override
    public void markRead(Message message) {
        if (!message.getSender().isMe()) {
            try {
                Stanza ack = ReadReceiptHelper.getReadAck(message.getEntityID(), message.getThread());

                XMPPManager.shared().getConnection().sendStanza(ack);
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            }
        }
    }

    @Override
    public void markDelivered(Message message) {
        if (!message.getSender().isMe()) {
            try {
                Stanza ack = ReadReceiptHelper.getAck(message.getEntityID(), message.getThread());

                XMPPManager.shared().getConnection().sendStanza(ack);
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            }
        }
    }

}
