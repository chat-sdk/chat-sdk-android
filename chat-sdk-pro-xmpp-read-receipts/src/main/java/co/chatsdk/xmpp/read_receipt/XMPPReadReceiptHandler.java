package co.chatsdk.xmpp.read_receipt;

import org.jivesoftware.smack.packet.Stanza;
import org.joda.time.DateTime;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.xmpp.XMPPManager;
import sdk.chat.core.types.ReadStatus;

public class XMPPReadReceiptHandler implements ReadReceiptHandler {

    @Override
    public void markRead(Message message) {
        // TODO: Check this! we only want to mark read if necessary
        if (!message.getSender().isMe()) {
            try {
                Stanza ack = ReadReceiptHelper.getReadAck(message.getEntityID(), message.getThread());
                XMPPManager.shared().getConnection().sendStanza(ack);
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            } finally {
                message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new DateTime());
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
            } finally {
                message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.delivered(), new DateTime());
            }
        }
    }

}
