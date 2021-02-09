package app.xmpp.adapter.listeners;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jxmpp.jid.Jid;

import java.util.Date;

import app.xmpp.adapter.defines.XMPPDefines;
import app.xmpp.adapter.utils.XMPPMessageWrapper;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;

public class XMPPReceiptReceivedListener implements ReceiptReceivedListener {
    @Override
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        if (ChatSDK.readReceipts() != null) {
            Message message = ChatSDK.db().fetchEntityWithEntityID(receiptId, Message.class);
            if (message != null) {
                XMPPMessageWrapper xmr = new XMPPMessageWrapper(receipt);
                String jid = xmr.userEntityID();
                if (jid != null) {
                    User user = ChatSDK.db().fetchUserWithEntityID(jid);
                    if (user != null) {
                        if (receipt.hasExtension(XMPPDefines.MessageReadNamespace)) {
                            message.setUserReadStatus(user, ReadStatus.read(), new Date());
                        } else {
                            message.setUserReadStatus(user, ReadStatus.delivered(), new Date());
                        }
                    }
                }
            }
        }
    }
}
