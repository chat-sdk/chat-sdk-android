package co.chatsdk.xmpp.read_receipt;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.joda.time.DateTime;
import org.jxmpp.jid.Jid;

import java.lang.ref.WeakReference;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.ReadReceiptHandler;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
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
