package app.xmpp.receipts;

import org.jivesoftware.smack.packet.Stanza;


import java.util.Date;

import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.ReadReceiptHandler;
import sdk.chat.core.session.ChatSDK;
import app.xmpp.adapter.XMPPManager;
import sdk.chat.core.types.ReadStatus;

public class XMPPReadReceiptHandler implements ReadReceiptHandler {

    @Override
    public void markRead(Message message) {
        // TODO: Check this! we only want to mark read if necessary
        if (!message.getSender().isMe()) {
            try {
                Stanza ack = ReadReceiptHelper.getReadAck(message.getEntityID(), message.getThread());
                XMPPManager.shared().sendStanza(ack);
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            } finally {
                message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.read(), new Date());
            }
        } else {
            message.setIsRead(true, true, true);
        }
    }

    @Override
    public void markDelivered(Message message) {
        if (!message.getSender().isMe()) {
            try {
                Stanza ack = ReadReceiptHelper.getAck(message.getEntityID(), message.getThread());
                XMPPManager.shared().sendStanza(ack);
            } catch (Exception e) {
                ChatSDK.events().onError(e);
            } finally {
                message.setUserReadStatus(ChatSDK.currentUser(), ReadStatus.delivered(), new Date());
            }
        }
    }

}
