package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.joda.time.DateTime;
import org.jxmpp.jid.Jid;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ReadStatus;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.defines.XMPPDefines;
import co.chatsdk.xmpp.module.XMPPModule;

public class XMPPReceiptReceivedListener implements ReceiptReceivedListener {
    @Override
    public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
        if (ChatSDK.readReceipts() != null) {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(fromJid.asBareJid().toString());
            if (thread != null) {
                Message message = thread.getMessageWithEntityID(receiptId);
                // Get the user...
                if (message != null && message.getSender().isMe()) {

                    User from = null;
                    if (thread.typeIs(ThreadType.Private1to1)) {
                        from = ChatSDK.db().fetchUserWithEntityID(fromJid.asBareJid().toString());
                    } else if (thread.typeIs(ThreadType.Group)) {
                        try {
                            MultiUserChat chat = XMPPManager.shared().mucManager.getRoom(thread.getEntityID());
                            String userEntityID = XMPPManager.shared().mucManager.userJID(chat, fromJid);
                            from = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, userEntityID);
                        } catch (Exception e) {
                            ChatSDK.events().onError(e);
                        }
                    }
                    if (from != null) {
                        if (receipt.hasExtension(XMPPDefines.MessageReadNamespace)) {
                            message.setUserReadStatus(from, ReadStatus.read(), new DateTime());
                        } else {
                            message.setUserReadStatus(from, ReadStatus.delivered(), new DateTime());
                        }
                    }
                }
            }
        }
    }
}
