package co.chatsdk.xmpp.read_receipt;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jxmpp.jid.impl.JidCreate;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.defines.XMPPDefines;

public class ReadReceiptHelper {

    public static Message getAck (String messageId, Thread thread) throws Exception {

        Message ack = new Message();

        if (thread.typeIs(ThreadType.Group)) {
            MultiUserChat chat = XMPPManager.shared().mucManager.chatForThreadID(thread.getEntityID());
            ack.setTo(chat.getRoom());
            ack.setType(Message.Type.groupchat);
        } else {
            ack.setType(Message.Type.chat);
            ack.setTo(JidCreate.from(thread.getEntityID()));
        }

        ack.addExtension(new DeliveryReceipt(messageId));

        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace).build();
        ack.addExtension(extension);

        return ack;
    }

    public static Message getReadAck (String messageId, Thread thread) throws Exception {
        Message ack = getAck(messageId, thread);
        ExtensionElement extension = StandardExtensionElement.builder(XMPPDefines.Extras, XMPPDefines.MessageReadNamespace).build();
        ack.addExtension(extension);
        return ack;
    }
}
