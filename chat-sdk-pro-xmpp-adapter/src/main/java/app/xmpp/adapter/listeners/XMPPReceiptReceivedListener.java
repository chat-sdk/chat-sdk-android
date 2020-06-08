package co.chatsdk.xmpp.listeners;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import org.jxmpp.jid.Jid;

import java.util.Date;

import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ReadStatus;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.defines.XMPPDefines;

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
                            message.setUserReadStatus(from, ReadStatus.read(), new Date());
                        } else {
                            message.setUserReadStatus(from, ReadStatus.delivered(), new Date());
                        }
                    }
                }
            }
        }
    }
}
